#  #%L
#  Alfresco HX Insight Connector
#  %%
#  Copyright (C) 2024 Alfresco Software Limited
#  %%
#  This file is part of the Alfresco software.
#  If the software was purchased under a paid Alfresco license, the terms of
#  the paid license agreement will prevail.  Otherwise, the software is
#  provided under the following open source license terms:
#
#  Alfresco is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Lesser General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  Alfresco is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License
#  along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
#  #L%

import sys
import base64
import requests
import json

PAGE_SIZE = 100
ENDPOINT = "/alfresco/api/-default-/public/alfresco/versions/1/types"

console_arguments = sys.argv[1:]


def is_input_valid():
    return len(console_arguments) == 3


def should_print_help():
    return is_input_valid() or console_arguments[0] in ["help", "-h", "--help"]


def get_host():
    return console_arguments[0]


def get_auth_token():
    username = console_arguments[1]
    password = console_arguments[2]

    return base64.b64encode(f"{username}:{password}".encode()).decode()


def is_status_success(status):
    return 200 <= status < 300


def get_namespace_to_prefix_mapping(type_info):
    model_info = type_info["entry"]["model"]

    return (model_info["namespaceUri"], model_info["namespacePrefix"])


def get_namespace_to_prefix_mappings(types_info):
    return dict(map(get_namespace_to_prefix_mapping, types_info))


def get_types_info(host, token, page):
    skip_count = (page - 1) * PAGE_SIZE

    url = f"http://{host}{ENDPOINT}?skipCount={skip_count}&maxItems={PAGE_SIZE}"
    headers = {
        'Accept': 'application/json',
        'Authorization': f'Basic {token}'
    }

    response = requests.request("GET", url, headers=headers, data={})

    if not is_status_success(response.status_code):
        print(response.text)
        exit(1)

    response_json = response.json()

    total_items = response_json["list"]["pagination"]["totalItems"]
    fetched_items = response_json["list"]["pagination"]["skipCount"] + response_json["list"]["pagination"]["count"]
    has_next_page = fetched_items < total_items

    return {
        "has_next_page": has_next_page,
        "next_page": lambda: get_types_info(host, token, page + 1),
        "types": get_namespace_to_prefix_mappings(response.json()["list"]["entries"])
    }


if __name__ == "__main__":
    if should_print_help():
        print(
            "---- Help ---- \n"
            "\n"
            "Usage: \n"
            "namespaces-to-namespace-prefixes-file-generator.py [alfresco_repository_host] [username] [password] \n"
            "\n"
            "Example: \n"
            "namespaces-to-namespace-prefixes-file-generator.py localhost:8080 admin admin"
            "\n"
        )

        exit(0)

    prefix_map = {}

    host = get_host()
    token = get_auth_token()

    types_info_response = get_types_info(host, token, 1)

    while True:
        prefix_map.update(types_info_response["types"])

        if not types_info_response["has_next_page"]:
            break

        types_info_response = types_info_response["next_page"]()

    out_filename = "namespace-prefixes.json"

    with open(out_filename, "w") as namespaces_file:
        json.dump({"prefixUriMap": prefix_map}, namespaces_file, indent=2)

    print(f"Done! You'll find generated mapping in {out_filename} file")
