# Third-Party License Texts

This directory contains local copies of the license texts for many of our third-party
dependencies. They are used by the `license-maven-plugin` to speed up the build
(via `licenseUrlReplacements` in the root `pom.xml`) so that license files are
resolved locally rather than downloaded from the internet.

## GPL / LGPL licenses

Some dependencies are dual-licensed under both a permissive license (EPL, EDL)
and a copyleft license (GPL, LGPL). In all such cases this project uses the
permissive alternative. For example `ch.qos.logback:logback-core` is licensed
with a choice of LGPL-2.1 or EPL-1.0 and we use EPL-1.0.

The `gpl-2.0-ce.txt` and `lgpl-2.1.txt` files are present only because the
`download-licenses` goal archives every license URL listed in a dependency's POM,
including the ones we do not use.

## Placeholder values

Some license texts (e.g. BSD-2-Clause, BSD-3-Clause, MIT) are SPDX templates
that contain placeholders such as `<year>` and `<owner>`. The actual copyright
holders and years for each dependency can be found in the NOTICE or LICENSE file
bundled inside the dependency's JAR.
