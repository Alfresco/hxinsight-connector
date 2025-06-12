#!/usr/bin/env python3

import os
import sys
import xml.etree.ElementTree as ET
import glob
import re
import subprocess
import datetime
from github import Github
from collections import defaultdict

# Constants - coverage thresholds
MIN_COVERAGE_OVERALL = 80.0
MIN_COVERAGE_CHANGED_FILES = 80.0

def get_env_variable(name, default=None, required=False):
    """Get environment variable with logging and validation."""
    value = os.environ.get(name, default)
    if required and not value:
        print(f"ERROR: Required environment variable '{name}' is not set")
        return None

    if value:
        if 'TOKEN' in name or 'PASSWORD' in name:
            print(f"Environment variable '{name}' is set")
        else:
            print(f"Environment variable '{name}' = '{value}'")
    else:
        print(f"Environment variable '{name}' is not set, using default: '{default}'")

    return value

def parse_jacoco_xml(xml_path):
    """Parse a JaCoCo XML report and extract coverage data."""
    try:
        print(f"Parsing JaCoCo XML file: {xml_path}")
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

        print(f"Successfully parsed {xml_path} - Found {len(file_coverage)} files with coverage data")
        return {
            'overall': overall_coverage,
            'files': file_coverage
        }

    except Exception as e:
        print(f"Error parsing JaCoCo XML file {xml_path}: {str(e)}")
        return {'overall': {}, 'files': {}}

def get_git_changed_files():
    """Get changed files using git commands."""
    try:
        print("Getting changed files using git...")

        base_branch = get_env_variable('GITHUB_BASE_REF', None)
        pr_number = get_env_variable('PR_NUMBER', None)

        if pr_number and base_branch:
            print(f"PR detected (#{pr_number}). Getting files changed between {base_branch} and current branch")
            cmd = ['git', 'diff', '--name-only', f'origin/{base_branch}...HEAD']
        else:
            print("Push event: Getting files changed in the last commit")
            cmd = ['git', 'diff', '--name-only', 'HEAD^', 'HEAD']

        print(f"Running command: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True)

        if result.returncode != 0:
            print(f"Error getting changed files with first approach: {result.stderr}")

            cmd = ['git', 'diff', '--name-only', 'HEAD~10', 'HEAD']
            print(f"Attempting alternative approach: {' '.join(cmd)}")
            result = subprocess.run(cmd, capture_output=True, text=True)

            if result.returncode != 0:
                print(f"Error getting changed files with second approach: {result.stderr}")
                return []

        changed_files = [file for file in result.stdout.splitlines() if file.endswith('.java')]

        print(f"Found {len(changed_files)} changed Java files")
        if changed_files:
            print("Changed Java files:")
            for file in changed_files[:10]:
                print(f"  - {file}")

            if len(changed_files) > 10:
                print(f"  ... and {len(changed_files) - 10} more files")

        return changed_files
    except Exception as e:
        print(f"ERROR: Failed to get changed files: {str(e)}")
        return []

def match_changed_files_to_coverage(changed_files, coverage_data):
    """Match changed files to coverage data."""
    print("Matching changed files to coverage data...")
    matches = []
    unmatched = []

    changed_file_names = [(f, os.path.basename(f).lower()) for f in changed_files]
    print(f"Processing {len(changed_files)} changed files against {len(coverage_data)} coverage reports")

    for changed_file, short_name in changed_file_names:
        found = False

        for report_index, report_coverage in enumerate(coverage_data):
            files_coverage = report_coverage.get('files', {})

            for coverage_path, coverage_info in files_coverage.items():
                if changed_file.replace('\\', '/') in coverage_path:
                    matches.append((changed_file, coverage_info))
                    found = True
                    print(f"Match found for {changed_file} in report #{report_index+1}")
                    break

                if short_name == os.path.basename(coverage_path).lower():
                    matches.append((changed_file, coverage_info))
                    found = True
                    print(f"Match found for {short_name} in report #{report_index+1}")
                    break

            if found:
                break

        if not found:
            unmatched.append(changed_file)

    print(f"Matched {len(matches)} of {len(changed_files)} changed files to coverage data")

    if unmatched:
        print("Unmatched files:")
        for file in unmatched:
            print(f"  - {file}")

    return matches

def calculate_overall_coverage(coverage_data, counter_type='LINE'):
    """Calculate overall coverage from all reports."""
    print(f"Calculating overall {counter_type} coverage from {len(coverage_data)} reports...")
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

    print(f"Overall {counter_type} coverage: covered={total_covered}, missed={total_missed}, percentage={overall_coverage:.2f}%")
    return overall_coverage

def calculate_changed_files_coverage(matched_files, counter_type='LINE'):
    """Calculate coverage for changed files."""
    print(f"Calculating {counter_type} coverage for {len(matched_files)} matched files...")
    if not matched_files:
        print("No matched files, returning 0% coverage")
        return 0

    total_covered = 0
    total_missed = 0

    for file_path, coverage_info in matched_files:
        if counter_type in coverage_info['counters']:
            file_covered = coverage_info['counters'][counter_type]['covered']
            file_missed = coverage_info['counters'][counter_type]['missed']
            total_covered += file_covered
            total_missed += file_missed
            file_percentage = coverage_info['counters'][counter_type]['percentage']
            print(f"  - {os.path.basename(file_path)}: covered={file_covered}, missed={file_missed}, percentage={file_percentage:.2f}%")

    if total_covered + total_missed > 0:
        coverage = (total_covered / (total_covered + total_missed)) * 100
    else:
        coverage = 0

    print(f"Changed files {counter_type} coverage: covered={total_covered}, missed={total_missed}, percentage={coverage:.2f}%")
    return coverage

def format_coverage_value(value):
    """Format coverage percentage value."""
    return f"{value:.2f}%"

def get_coverage_emoji(value, threshold):
    """Get emoji based on coverage threshold."""
    return ":green_circle:" if value >= threshold else ":red_circle:"

def create_pr_comment(overall_coverage, changed_files_coverage, matched_files):
    """Create a PR comment with coverage information and save to file."""
    print("Creating coverage report...")
    overall_emoji = get_coverage_emoji(overall_coverage, MIN_COVERAGE_OVERALL)

    comment = "# Code Coverage Report\n\n"
    comment += "| Type | Coverage | Status |\n"
    comment += "|------|----------|--------|\n"
    comment += f"| Overall | {format_coverage_value(overall_coverage)} | {overall_emoji} |\n"

    if matched_files:
        changed_emoji = get_coverage_emoji(changed_files_coverage, MIN_COVERAGE_CHANGED_FILES)
        comment += f"| Files changed | {format_coverage_value(changed_files_coverage)} | {changed_emoji} |\n"

        comment += "\n"

        comment += "\n## Files Changed\n\n"
        comment += "| File | Coverage | Status |\n"
        comment += "|------|----------|--------|\n"

        for file_path, coverage_info in matched_files:
            file_name = os.path.basename(file_path)

            line_coverage = coverage_info['counters']['LINE']['percentage'] if 'LINE' in coverage_info['counters'] else 0
            line_covered = coverage_info['counters']['LINE']['covered'] if 'LINE' in coverage_info['counters'] else 0
            line_total = coverage_info['counters']['LINE']['total'] if 'LINE' in coverage_info['counters'] else 0

            status = get_coverage_emoji(line_coverage, MIN_COVERAGE_CHANGED_FILES)

            comment += f"| {file_name} | {format_coverage_value(line_coverage)} ({line_covered}/{line_total}) | {status} |\n"
    elif not matched_files:
        comment += "\n_There is no coverage information present for the Files changed._\n"
    with open('coverage-report-comment.md', 'w') as f:
        f.write(comment)
    print("Coverage report saved to coverage-report-comment.md")

    return comment

def set_output(name, value):
    """Set GitHub Actions output variable."""
    github_output = get_env_variable('GITHUB_OUTPUT')

    if github_output:
        with open(github_output, 'a') as f:
            f.write(f"{name}={value}\n")
    else:
        with open('coverage-output.txt', 'a') as f:
            f.write(f"{name}={value}\n")
        print(f"Set local output '{name}={value}' in coverage-output.txt")

def publish_pr_comment(comment_content, pr_number, repository_name):
    """Publish a comment to a GitHub PR."""
    if not pr_number:
        print("No PR number provided, skipping comment creation")
        return None

    try:
        print(f"Publishing comment to PR #{pr_number}")
        github_token = get_env_variable('GITHUB_TOKEN', required=True)
        if not github_token:
            print("ERROR: GITHUB_TOKEN environment variable not set")
            return None

        g = Github(github_token)
        repo = g.get_repo(repository_name)
        pr = repo.get_pull(int(pr_number))

        existing_comments = list(pr.get_issue_comments())
        comment_id = None

        for comment in existing_comments:
            if comment.user.login == 'github-actions[bot]' and '# Code Coverage Report' in comment.body:
                comment_id = comment.id
                print(f"Found existing coverage comment with ID: {comment_id}")
                break

        if comment_id:
            comment = pr.get_issue_comment(comment_id)
            comment.edit(comment_content)
            print("Updated existing coverage comment")
            return comment
        else:
            comment = pr.create_issue_comment(comment_content)
            print("Created new coverage comment")
            return comment

    except Exception as e:
        print(f"ERROR: Failed to publish PR comment: {str(e)}")
        return None

def main():
    print("=== Code Coverage Report Processing ===")

    workspace = get_env_variable('WORKSPACE', os.environ.get('GITHUB_WORKSPACE', os.getcwd()))
    print(f"Using workspace directory: {workspace}")

    reports = glob.glob(f"{workspace}/**/target/site/jacoco/jacoco.xml", recursive=True)

    if not reports:
        print("ERROR: No JaCoCo reports found")
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

    changed_files_coverage = 100.0
    matched_files = []

    try:
        changed_files = get_git_changed_files()
        if changed_files:
            matched_files = match_changed_files_to_coverage(changed_files, coverage_data)
            if matched_files:
                changed_files_coverage = calculate_changed_files_coverage(matched_files)
                print(f"Changed files coverage: {format_coverage_value(changed_files_coverage)}")
            else:
                print("No matched files found for coverage analysis, setting changed files coverage to 100%")
        else:
            print("No Java files were changed, setting changed files coverage to 100%")
    except Exception as e:
        print(f"ERROR: Failed to calculate changed files coverage: {str(e)}")
        print("Setting changed files coverage to 100%")

    comment = create_pr_comment(overall_coverage, changed_files_coverage, matched_files)

    set_output("coverage-overall", overall_coverage)
    set_output("coverage-changed-files", changed_files_coverage)

    pr_number = get_env_variable('PR_NUMBER')
    repo_name = get_env_variable('GITHUB_REPOSITORY')

    if pr_number and repo_name:
        if overall_coverage < MIN_COVERAGE_OVERALL or changed_files_coverage < MIN_COVERAGE_CHANGED_FILES:
            warning_message = "\n\n### Coverage Threshold Alert \n\n"

            if overall_coverage < MIN_COVERAGE_OVERALL:
                warning_message += f"- Overall coverage ({format_coverage_value(overall_coverage)}) is below threshold ({format_coverage_value(MIN_COVERAGE_OVERALL)})\n"

            if changed_files_coverage < MIN_COVERAGE_CHANGED_FILES:
                warning_message += f"- Changed files coverage ({format_coverage_value(changed_files_coverage)}) is below threshold ({format_coverage_value(MIN_COVERAGE_CHANGED_FILES)})\n"

            comment += warning_message

            publish_pr_comment(comment, pr_number, repo_name)
            print("Published coverage report comment to PR due to coverage below thresholds")
        else:
            print("Coverage thresholds met, skipping PR comment creation")

    if overall_coverage < MIN_COVERAGE_OVERALL:
        print(f"WARNING: Overall coverage {overall_coverage:.2f}% is below threshold {MIN_COVERAGE_OVERALL}%")

    if changed_files_coverage < MIN_COVERAGE_CHANGED_FILES:
        print(f"WARNING: Changed files coverage {changed_files_coverage:.2f}% is below threshold {MIN_COVERAGE_CHANGED_FILES}%")
    print("Successfully completed coverage report processing")
    return 0

if __name__ == "__main__":
    try:
        exit_code = main()
        print(f"Exiting with code: {exit_code}")
        sys.exit(exit_code)
    except Exception as e:
        print(f"ERROR: Uncaught exception in main(): {str(e)}")
        sys.exit(1)
