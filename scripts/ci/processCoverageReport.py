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

def get_env_variable(name, default=None, required=False, log_value=False):
    """Get environment variable with logging and validation."""
    value = os.environ.get(name, default)
    if required and not value:
        print(f"ERROR: Required environment variable '{name}' is not set")
        return None

    if log_value:
        if value:
            if 'TOKEN' in name or 'PASSWORD' in name:
                print(f"Environment variable '{name}' is set")
            else:
                print(f"Environment variable '{name}' = '{value}'")
        else:
            print(f"Environment variable '{name}' is not set, using default: '{default}'")

    return value

def parse_jacoco_xml(xml_path, changed_files=None):
    """Parse a JaCoCo XML report and extract coverage data."""
    try:
        print(f"Parsing JaCoCo XML file: {xml_path}")
        tree = ET.parse(xml_path)
        root = tree.getroot()

        covered_lines = 0
        missed_lines = 0
        covered_lines_from_changed_files = 0
        missed_lines_from_changed_files = 0
        file_coverage_details = {}

        for counter in root.findall(".//counter[@type='LINE']"):
            # JaCoCo counter types include: INSTRUCTION, BRANCH, LINE, COMPLEXITY, METHOD, CLASS
            # INSTRUCTION - Java bytecode instructions
            # BRANCH - Branch coverage (if-else, switch statements)
            # LINE - Source code line coverage
            # COMPLEXITY - Cyclomatic complexity
            # METHOD - Method coverage
            # CLASS - Class coverage
            covered_lines += int(counter.get('covered', 0))
            missed_lines += int(counter.get('missed', 0))

        total_lines = covered_lines + missed_lines

        if changed_files:
            normalized_changed_files = [f.replace('\\', '/').lower() for f in changed_files]

            for package in root.findall(".//package"):
                package_name = package.get('name', '').replace('/', '.')

                for cls in package.findall("./class"):
                    source_file = cls.get('sourcefilename', '')
                    if not source_file:
                        continue

                    file_path = f"{package_name.replace('.', '/')}/{source_file}"

                    is_changed_file = any(
                        file_path.lower() == changed_file or
                        file_path.lower().endswith('/' + changed_file) or
                        changed_file.endswith('/' + file_path.lower())
                        for changed_file in normalized_changed_files
                    )

                    if is_changed_file:
                        for counter in cls.findall("./counter[@type='LINE']"):
                            file_covered = int(counter.get('covered', 0))
                            file_missed = int(counter.get('missed', 0))

                            covered_lines_from_changed_files += file_covered
                            missed_lines_from_changed_files += file_missed

                            if file_path not in file_coverage_details:
                                file_coverage_details[file_path] = {'covered': 0, 'missed': 0}
                            file_coverage_details[file_path]['covered'] += file_covered
                            file_coverage_details[file_path]['missed'] += file_missed

        total_lines_from_changed_files = covered_lines_from_changed_files + missed_lines_from_changed_files

        print(f"Successfully parsed {xml_path} - Overall: {covered_lines}/{total_lines} lines covered, Changed files: {covered_lines_from_changed_files}/{total_lines_from_changed_files} lines covered")

        return {
            'covered_lines': covered_lines,
            'total_lines': total_lines,
            'covered_lines_from_changed_files': covered_lines_from_changed_files,
            'total_lines_from_changed_files': total_lines_from_changed_files,
            'file_coverage_details': file_coverage_details
        }

    except Exception as e:
        print(f"Error parsing JaCoCo XML file {xml_path}: {str(e)}")
        return {
            'covered_lines': 0,
            'total_lines': 0,
            'covered_lines_from_changed_files': 0,
            'total_lines_from_changed_files': 0,
            'file_coverage_details': {}
        }

def calculate_file_coverage(file_path, coverage_data):
    """Calculate coverage for a specific file."""
    total_covered = 0
    total_missed = 0

    for data in coverage_data:
        file_details = data.get('file_coverage_details', {})
        for covered_file_path, coverage_info in file_details.items():
            if os.path.basename(file_path).lower() == os.path.basename(covered_file_path).lower():
                total_covered += coverage_info.get('covered', 0)
                total_missed += coverage_info.get('missed', 0)

    total_lines = total_covered + total_missed
    if total_lines == 0:
        return 100.0

    return (total_covered / total_lines) * 100

def get_git_changed_files():
    """Get changed files using git commands."""
    try:
        print("Getting changed files using git...")

        pr_number = get_env_variable('PR_NUMBER', None)

        if pr_number:
            print(f"PR detected (#{pr_number}). Getting files changed between master and current branch")
            cmd = ['git', 'diff', '--name-only', 'origin/master...HEAD']
        else:
            print("Push event: Getting files changed in the last commit")
            cmd = ['git', 'diff', '--name-only', 'HEAD^', 'HEAD']

        print(f"Running command: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True)

        changed_files = [file for file in result.stdout.splitlines() if file.endswith('.java') and file.endswith('Test.java') is False]

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

    total_changed_lines = sum(data.get('total_lines_from_changed_files', 0) for data in coverage_data)

    if total_changed_lines > 0:
        for file in changed_files:
            matches.append((file, {}))
        print(f"Found coverage data for changed files. Matched {len(matches)} files")
    else:
        print("No coverage data found for changed files")

    print(f"Matched {len(matches)} of {len(changed_files)} changed files to coverage data")
    return matches

def calculate_overall_coverage(coverage_data, counter_type='LINE'):
    """Calculate overall coverage from all reports."""
    print(f"Calculating overall {counter_type} coverage from {len(coverage_data)} reports...")
    total_covered = 0
    total_missed = 0

    for data in coverage_data:
        if counter_type == 'LINE':
            total_covered += data.get('covered_lines', 0)
            total_missed += data.get('total_lines', 0) - data.get('covered_lines', 0)

    total_lines = total_covered + total_missed

    if total_lines == 0:
        overall_coverage = 100.0
    else:
        overall_coverage = (total_covered / total_lines) * 100

    print(f"Overall {counter_type} coverage: covered={total_covered}, missed={total_missed}, percentage={overall_coverage:.2f}%")
    return overall_coverage

def calculate_changed_files_coverage(coverage_data, counter_type='LINE'):
    """Calculate coverage for changed files using coverage_data."""
    print(f"Calculating {counter_type} coverage for changed files...")

    total_covered = 0
    total_missed = 0

    for data in coverage_data:
        if counter_type == 'LINE':
            covered_from_changed = data.get('covered_lines_from_changed_files', 0)
            total_from_changed = data.get('total_lines_from_changed_files', 0)
            missed_from_changed = total_from_changed - covered_from_changed

            total_covered += covered_from_changed
            total_missed += missed_from_changed

    total_lines = total_covered + total_missed

    if total_lines == 0:
        coverage = 100.0
    else:
        coverage = (total_covered / total_lines) * 100

    print(f"Changed files {counter_type} coverage: covered={total_covered}, missed={total_missed}, percentage={coverage:.2f}%")
    return coverage

def format_coverage_value(value):
    """Format coverage percentage value."""
    return f"{value:.2f}%"

def get_coverage_emoji(value, threshold):
    """Get emoji based on coverage threshold."""
    return ":green_circle:" if value >= threshold else ":red_circle:"

def create_pr_comment(overall_coverage, changed_files_coverage, matched_files, coverage_data=None):
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

        for file_path, _ in matched_files:
            file_name = os.path.basename(file_path)
            file_coverage = calculate_file_coverage(file_path, coverage_data) if coverage_data else 0.0
            status = get_coverage_emoji(file_coverage, MIN_COVERAGE_CHANGED_FILES)
            comment += f"| {file_name} | {format_coverage_value(file_coverage)} | {status} |\n"
    else:
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

    changed_files = get_git_changed_files()

    coverage_data = []
    for report in reports:
        report_data = parse_jacoco_xml(report, changed_files)
        coverage_data.append(report_data)

    overall_coverage = calculate_overall_coverage(coverage_data, counter_type='LINE')
    print(f"Overall line coverage: {format_coverage_value(overall_coverage)}")

    changed_files_coverage = 100.0
    matched_files = []

    try:
        if changed_files:
            matched_files = match_changed_files_to_coverage(changed_files, coverage_data)
            if matched_files or any(data.get('total_lines_from_changed_files', 0) > 0 for data in coverage_data):
                changed_files_coverage = calculate_changed_files_coverage(coverage_data, counter_type='LINE')
                print(f"Changed files coverage: {format_coverage_value(changed_files_coverage)}")
            else:
                print("No matched files found for coverage analysis, setting changed files coverage to 100%")
        else:
            print("No Java files were changed, setting changed files coverage to 100%")
    except Exception as e:
        print(f"ERROR: Failed to calculate changed files coverage: {str(e)}")
        print("Setting changed files coverage to 100%")

    comment = create_pr_comment(overall_coverage, changed_files_coverage, matched_files, coverage_data)

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
