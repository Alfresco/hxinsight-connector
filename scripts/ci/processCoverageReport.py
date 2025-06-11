#!/usr/bin/env python3

import os
import sys
import xml.etree.ElementTree as ET
import glob
import json
import requests
from github import Github
import re

# Constants - coverage thresholds
MIN_COVERAGE_OVERALL = 80.0
MIN_COVERAGE_CHANGED_FILES = 80.0

def parse_jacoco_xml(xml_path):
    """Parse a JaCoCo XML report and extract coverage data."""
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()

        counter_elements = root.findall(".//counter")
        coverage_data = {}

        packages = {}
        for package_elem in root.findall(".//package"):
            package_name = package_elem.get('name', '')

            for class_elem in package_elem.findall("./class"):
                class_name = class_elem.get('name', '')
                source_file = class_elem.get('sourcefilename', '')

                class_counters = {}
                for counter in class_elem.findall("./counter"):
                    type_name = counter.get('type')
                    covered = int(counter.get('covered', 0))
                    missed = int(counter.get('missed', 0))

                    class_counters[type_name] = {
                        'covered': covered,
                        'missed': missed,
                        'total': covered + missed,
                        'percentage': (covered / (covered + missed) * 100) if (covered + missed) > 0 else 0
                    }

                full_path = f"{package_name.replace('.', '/')}/{source_file}"
                packages[full_path] = {
                    'package': package_name,
                    'class': class_name,
                    'counters': class_counters
                }

        for counter in counter_elements:
            type_name = counter.get('type')
            covered = int(counter.get('covered', 0))
            missed = int(counter.get('missed', 0))

            if covered + missed > 0:
                coverage_percent = (covered / (covered + missed)) * 100
            else:
                coverage_percent = 0

            coverage_data[type_name] = {
                'covered': covered,
                'missed': missed,
                'total': covered + missed,
                'percentage': coverage_percent
            }

        return {'overall': coverage_data, 'files': packages}
    except Exception as e:
        print(f"Error parsing JaCoCo XML file {xml_path}: {str(e)}")
        return {'overall': {}, 'files': {}}

def get_changed_files():
    """Get the list of changed files in the PR."""
    try:
        token = os.environ.get('GITHUB_TOKEN')
        repo = os.environ.get('GITHUB_REPOSITORY')
        pr_number = os.environ.get('PR_NUMBER')

        if not all([token, repo, pr_number]):
            print("Missing required environment variables for getting changed files")
            return []

        g = Github(token)
        repo = g.get_repo(repo)
        pr = repo.get_pull(int(pr_number))

        changed_files = [file.filename for file in pr.get_files() if file.filename.endswith('.java')]
        print(f"Found {len(changed_files)} changed Java files in PR")
        return changed_files
    except Exception as e:
        print(f"Error getting changed files: {str(e)}")
        return []

def calculate_overall_coverage(all_coverage_data):
    """Calculate overall coverage from all reports."""
    total_covered = 0
    total_missed = 0

    for data in all_coverage_data:
        if 'overall' in data and 'LINE' in data['overall']:
            total_covered += data['overall']['LINE']['covered']
            total_missed += data['overall']['LINE']['missed']

    if total_covered + total_missed > 0:
        overall_coverage = (total_covered / (total_covered + total_missed)) * 100
    else:
        overall_coverage = 0

    return overall_coverage

def calculate_changed_files_coverage(all_coverage_data, changed_files):
    """Calculate coverage for changed files."""
    if not changed_files:
        return calculate_overall_coverage(all_coverage_data)

    total_covered = 0
    total_missed = 0
    matched_files = set()

    source_paths = []
    for file_path in changed_files:
        if file_path.endswith('.java'):
            java_file = os.path.basename(file_path)
            source_paths.append(java_file)

    for data in all_coverage_data:
        if 'files' not in data:
            continue

        for file_path, file_info in data['files'].items():
            for source_path in source_paths:
                if source_path in file_path:
                    if 'LINE' in file_info.get('counters', {}):
                        total_covered += file_info['counters']['LINE']['covered']
                        total_missed += file_info['counters']['LINE']['missed']
                        matched_files.add(source_path)

    print(f"Matched {len(matched_files)} of {len(changed_files)} changed files in coverage data")

    if total_covered + total_missed == 0:
        print("No coverage data found for changed files, using overall coverage")
        return calculate_overall_coverage(all_coverage_data)

    changed_files_coverage = (total_covered / (total_covered + total_missed)) * 100
    return changed_files_coverage

def post_coverage_comment(overall_coverage, changed_files_coverage):
    """Post a coverage report comment to the PR."""
    try:
        token = os.environ.get('GITHUB_TOKEN')
        repo = os.environ.get('GITHUB_REPOSITORY')
        pr_number = os.environ.get('PR_NUMBER')

        if not all([token, repo, pr_number]):
            print("Missing required environment variables for posting PR comment")
            return False

        overall_emoji = ':green_circle:' if overall_coverage >= MIN_COVERAGE_OVERALL else ':red_circle:'
        changed_emoji = ':green_circle:' if changed_files_coverage >= MIN_COVERAGE_CHANGED_FILES else ':red_circle:'

        comment = f"""# Code Coverage Report

| Type | Coverage | Status |
| ---- | -------- | ------ |
| Overall | {overall_coverage:.2f}% | {overall_emoji} |
| Changed Files | {changed_files_coverage:.2f}% | {changed_emoji} |

"""

        g = Github(token)
        repo = g.get_repo(repo)
        pr = repo.get_pull(int(pr_number))
        pr.create_issue_comment(comment)

        return True
    except Exception as e:
        print(f"Error posting PR comment: {str(e)}")
        return False

def main():
    workspace = os.environ.get('GITHUB_WORKSPACE', '.')

    reports = glob.glob(f"{workspace}/**/target/site/jacoco/jacoco.xml", recursive=True)

    if not reports:
        print("No JaCoCo reports found")
        return 1

    print(f"Found {len(reports)} JaCoCo reports")

    all_coverage_data = []
    for report in reports:
        print(f"Parsing report: {report}")
        coverage_data = parse_jacoco_xml(report)
        all_coverage_data.append(coverage_data)

    overall_coverage = calculate_overall_coverage(all_coverage_data)
    print(f"Overall coverage: {overall_coverage:.2f}%")

    changed_files_coverage = overall_coverage
    if os.environ.get('GITHUB_EVENT_NAME') == 'pull_request':
        changed_files = get_changed_files()
        changed_files_coverage = calculate_changed_files_coverage(all_coverage_data, changed_files)
        print(f"Changed files coverage: {changed_files_coverage:.2f}%")

    if os.environ.get('GITHUB_EVENT_NAME') == 'pull_request':
        success = post_coverage_comment(overall_coverage, changed_files_coverage)
        if success:
            print("Successfully posted coverage comment to PR")
        else:
            print("Failed to post coverage comment to PR")

    with open(os.environ.get('GITHUB_OUTPUT', '/dev/null'), 'a') as f:
        f.write(f"coverage-overall={overall_coverage}\n")
        f.write(f"coverage-changed-files={changed_files_coverage}\n")

    if overall_coverage < MIN_COVERAGE_OVERALL:
        print(f"Overall coverage {overall_coverage:.2f}% is below threshold {MIN_COVERAGE_OVERALL}%")
        return 1

    if changed_files_coverage < MIN_COVERAGE_CHANGED_FILES:
        print(f"Changed files coverage {changed_files_coverage:.2f}% is below threshold {MIN_COVERAGE_CHANGED_FILES}%")
        return 1

    return 0

if __name__ == "__main__":
    sys.exit(main())
