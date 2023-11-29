# Alfresco HxInsight Connector

Connector for sending ACS events to HxInsight and updating the Repository with the predictions that it generates.


### Code quality
This project uses `pre-commit` and `clang-format` to ensure code quality. To run them locally you should:

(mac)
```bash
brew install pre-commit
brew install clang-format
```

Than install pre-commit in git repository
```bash
pre-commit install
```

Then to reformat all files run
```bash
pre-commit run --all-files
```
