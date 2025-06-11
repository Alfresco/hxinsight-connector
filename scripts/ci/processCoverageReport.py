#!/usr/bin/env python3

import os
import sys
import xml.etree.ElementTree as ET
import glob
import re
from github import Github
from collections import defaultdict

# Constants - coverage thresholds
MIN_COVERAGE_OVERALL = 80.0
MIN_COVERAGE_CHANGED_FILES = 80.0

def parse_jacoco_xml(xml_path):
    """Parse a JaCoCo XML report and extract coverage data."""
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()

        overall_coverage = {}
        for counter in root.findall(".//counter"):
            type_name = counter.get('type')
            covered = int(counter.get('covered', 0))
            missed = int(counter.get('missed', 0))

            overall_coverage[type_name] = {
                'covered': covered,
                'missed': missed,
                'total': covered + missed,
                'percentage': (covered / (covered + missed) * 100) if (covered + missed) > 0 else 0
            }

        file_coverage = {}

        for package in root.findall(".//package"):
            package_name = package.get('name', '').replace('/', '.')

            for cls in package.findall("./class"):
                class_name = cls.get('name', '')
                source_file = cls.get('sourcefilename', '')

                if not source_file:
                    continue

                file_path = f"{package_name.replace('.', '/')}/{source_file}"

                if file_path not in file_coverage:
                    file_coverage[file_path] = {
                        'package': package_name,
                        'source_file': source_file,
                        'full_path': file_path,
                        'counters': defaultdict(lambda: {'covered': 0, 'missed': 0})
                    }

                for counter in cls.findall("./counter"):
                    type_name = counter.get('type')
                    covered = int(counter.get('covered', 0))
                    missed = int(counter.get('missed', 0))

                    file_coverage[file_path]['counters'][type_name]['covered'] += covered
                    file_coverage[file_path]['counters'][type_name]['missed'] += missed

        for file_path, file_data in file_coverage.items():
            for counter_type, counter in file_data['counters'].items():
                total = counter['covered'] + counter['missed']
                counter['total'] = total
                counter['percentage'] = (counter['covered'] / total * 100) if total > 0 else 0

        return {
            'overall': overall_coverage,
            'files': file_coverage
        }

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

def match_changed_files_to_coverage(changed_files, coverage_data):
    """Match changed files to coverage data."""
    matches = []

    changed_file_names = [(f, os.path.basename(f).lower()) for f in changed_files]

    for report_coverage in coverage_data:
        files_coverage = report_coverage.get('files', {})

        for changed_file, short_name in changed_file_names:
            for coverage_path, coverage_info in files_coverage.items():
                if changed_file.replace('\\', '/') in coverage_path:
                    matches.append((changed_file, coverage_info))
                    break

                if short_name == os.path.basename(coverage_path).lower():
                    matches.append((changed_file, coverage_info))
                    break

    print(f"Matched {len(matches)} of {len(changed_files)} changed files to coverage data")
    return matches

def calculate_overall_coverage(coverage_data, counter_type='LINE'):
    """Calculate overall coverage from all reports."""
    total_covered = 0
    total_missed = 0

    for data in coverage_data:
        if 'overall' in data and counter_type in data['overall']:
            total_covered += data['overall'][counter_type]['covered']
            total_missed += data['overall'][counter_type]['missed']

    if total_covered + total_missed > 0:
        overall_coverage = (total_covered / (total_covered + total_missed)) * 100
    else:
        overall_coverage = 0

    return overall_coverage

def calculate_changed_files_coverage(matched_files, counter_type='LINE'):
    """Calculate coverage for changed files."""
    if not matched_files:
        return 0

    total_covered = 0
    total_missed = 0

    for _, coverage_info in matched_files:
        if counter_type in coverage_info['counters']:
            total_covered += coverage_info['counters'][counter_type]['covered']
            total_missed += coverage_info['counters'][counter_type]['missed']

    if total_covered + total_missed > 0:
        coverage = (total_covered / (total_covered + total_missed)) * 100
    else:
        coverage = 0

    return coverage

def format_coverage_value(value):
    """Format coverage percentage value."""
    return f"{value:.2f}%"

def get_coverage_emoji(value, threshold):
    """Get emoji based on coverage threshold."""
    return ":green_circle:" if value >= threshold else ":red_circle:"

def create_pr_comment(overall_coverage, changed_files_coverage, matched_files):
    """Create a PR comment with coverage information."""
    overall_emoji = get_coverage_emoji(overall_coverage, MIN_COVERAGE_OVERALL)
    changed_emoji = get_coverage_emoji(changed_files_coverage, MIN_COVERAGE_CHANGED_FILES)

    comment = f"""# Code Coverage Report

| Type | Coverage | Status |
|------|----------|--------|
| Overall | {format_coverage_value(overall_coverage)} | {overall_emoji} |
| Changed Files | {format_coverage_value(changed_files_coverage)} | {changed_emoji} |

"""

    if matched_files:
        comment += "\n## Changed Files\n\n"
        comment += "| File | Line Coverage | Branch Coverage | Status |\n"
        comment += "|------|---------------|----------------|--------|\n"

        for file_path, coverage_info in matched_files:
            line_coverage = coverage_info['counters']['LINE']['percentage'] if 'LINE' in coverage_info['counters'] else 0
            branch_coverage = coverage_info['counters']['BRANCH']['percentage'] if 'BRANCH' in coverage_info['counters'] else 0

            status = get_coverage_emoji(line_coverage, MIN_COVERAGE_CHANGED_FILES)

            file_name = os.path.basename(file_path)
            comment += f"| {file_name} | {format_coverage_value(line_coverage)} | {format_coverage_value(branch_coverage)} | {status} |\n"

    return comment

def post_coverage_comment(comment_text):
    """Post a coverage report comment to the PR."""
    try:
        token = os.environ.get('GITHUB_TOKEN')
        repo = os.environ.get('GITHUB_REPOSITORY')
        pr_number = os.environ.get('PR_NUMBER')

        if not all([token, repo, pr_number]):
            print("Missing required environment variables for posting PR comment")
            return False

        g = Github(token)
        repo = g.get_repo(repo)
        pr = repo.get_pull(int(pr_number))

        existing_comments = pr.get_issue_comments()
        for comment in existing_comments:
            if comment.user.login == 'github-actions[bot]' and '# Code Coverage Report' in comment.body:
                comment.edit(comment_text)
                print("Updated existing coverage comment")
                return True

        pr.create_issue_comment(comment_text)
        print("Posted new coverage comment")
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

    print(f"Found {len(reports)} JaCoCo reports:")
    for report in reports:
        print(f"  - {report}")

    coverage_data = []
    for report in reports:
        report_data = parse_jacoco_xml(report)
        coverage_data.append(report_data)

    overall_coverage = calculate_overall_coverage(coverage_data)
    print(f"Overall line coverage: {format_coverage_value(overall_coverage)}")

    changed_files_coverage = 0
    matched_files = []

    if os.environ.get('GITHUB_EVENT_NAME') == 'pull_request':
        changed_files = get_changed_files()
        matched_files = match_changed_files_to_coverage(changed_files, coverage_data)
        changed_files_coverage = calculate_changed_files_coverage(matched_files)
        print(f"Changed files coverage: {format_coverage_value(changed_files_coverage)}")

    if os.environ.get('GITHUB_EVENT_NAME') == 'pull_request':
        comment = create_pr_comment(overall_coverage, changed_files_coverage, matched_files)
        post_coverage_comment(comment)

    with open(os.environ.get('GITHUB_OUTPUT', '/dev/null'), 'a') as f:
        f.write(f"coverage-overall={overall_coverage}\n")
        f.write(f"coverage-changed-files={changed_files_coverage}\n")

    if overall_coverage < MIN_COVERAGE_OVERALL:
        print(f"Overall coverage {overall_coverage:.2f}% is below threshold {MIN_COVERAGE_OVERALL}%")
        return 1

    if os.environ.get('GITHUB_EVENT_NAME') == 'pull_request' and changed_files_coverage < MIN_COVERAGE_CHANGED_FILES:
        print(f"Changed files coverage {changed_files_coverage:.2f}% is below threshold {MIN_COVERAGE_CHANGED_FILES}%")
        return 1

    return 0

if __name__ == "__main__":
    sys.exit(main())
