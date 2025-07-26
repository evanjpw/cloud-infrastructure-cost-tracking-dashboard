# Commit Guidelines

This project follows [Conventional Commits](https://www.conventionalcommits.org/) specification for consistent commit messages and automated changelog generation.

## Quick Start

### Option 1: Use Commitizen (Recommended)
```bash
# Instead of `git commit`, use:
npm run commit
```
This will prompt you through creating a properly formatted commit message.

### Option 2: Manual Format
```bash
git commit -m "feat(frontend): add responsive sidebar navigation"
```

## Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type
Must be one of the following:

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **build**: Changes that affect the build system or external dependencies
- **ci**: Changes to CI configuration files and scripts
- **chore**: Other changes that don't modify src or test files
- **revert**: Reverts a previous commit

### Scope (optional)
The scope could be anything specifying place of the commit change:
- `frontend` - React components, styles, etc.
- `backend` - Spring Boot application, controllers, services
- `database` - Database schema, migrations, queries
- `docker` - Docker configurations, compose files
- `docs` - Documentation files
- `deps` - Dependency updates

### Subject
- Use the imperative, present tense: "change" not "changed" nor "changes"
- Don't capitalize the first letter
- No dot (.) at the end
- Maximum 50 characters

### Body (optional)
- Use the imperative, present tense
- Include motivation for the change and contrasts with previous behavior
- Wrap at 72 characters

### Footer (optional)
- Reference GitHub issues: `Fixes #123`, `Closes #456`
- Note breaking changes: `BREAKING CHANGE: describe the change`

## Examples

### Simple feature
```
feat(auth): add user login functionality
```

### Bug fix with scope
```
fix(database): resolve connection timeout issues

- Increase connection pool size to 20
- Add retry logic for failed connections
- Update timeout configuration to 30 seconds

Fixes #234
```

### Breaking change
```
feat(api): restructure cost report response format

BREAKING CHANGE: The cost report API now returns data in a different format.
The 'breakdowns' array has been moved to the root level instead of being
nested under a 'data' property.

Migration guide:
- Change `response.data.breakdowns` to `response.breakdowns`
```

### Documentation
```
docs(readme): update installation instructions

Add missing environment variables section and clarify
Docker Compose setup steps.
```

## CHANGELOG Management

### Automatic Generation
We use `standard-version` to automatically:
- Bump version numbers based on commit types
- Generate CHANGELOG.md entries
- Create git tags

### Creating a Release
```bash
# For automatic versioning based on commits
npm run release

# For specific version types
npm run release -- --release-as minor
npm run release -- --release-as major
npm run release -- --release-as 1.3.0
```

### Manual CHANGELOG Updates
```bash
# Generate changelog entries for unreleased commits
npm run changelog
```

## Workflow Integration

### Pre-commit Validation
The project includes commitlint to validate commit messages automatically:
- Install hooks: `npx husky install` (already done)
- Invalid commits will be rejected with helpful error messages

### IDE Integration
Consider installing IDE extensions:
- VS Code: "Conventional Commits"
- WebStorm: "Git Commit Template" plugin

## Best Practices

1. **One logical change per commit** - Don't mix unrelated changes
2. **Test before committing** - Ensure your changes work
3. **Write descriptive messages** - Future you will thank you
4. **Use present tense** - "add feature" not "added feature"
5. **Reference issues** - Connect commits to GitHub issues when relevant
6. **Review the diff** - Use `git diff --cached` before committing

## Common Patterns

### Feature Development
```bash
feat(frontend): add cost breakdown chart component
feat(backend): implement team filtering API endpoint
feat(database): add indexes for performance optimization
```

### Bug Fixes
```bash
fix(frontend): resolve mobile responsive layout issues
fix(backend): handle null values in cost calculations
fix(docker): update nginx configuration for proper routing
```

### Maintenance
```bash
chore(deps): update React to version 18.2.0
docs(architecture): add sequence diagrams for API flows
style(frontend): format components with prettier
```

## Troubleshooting

### Commit Rejected
If your commit is rejected by commitlint:
1. Check the error message for specific formatting issues
2. Use `npm run commit` for guided commit creation
3. Refer to this guide for proper format

### CHANGELOG Issues
- Ensure all commits follow conventional format
- Run `npm run changelog` to regenerate entries
- Manual edits to CHANGELOG.md are preserved during regeneration

## Resources

- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [Commitizen Documentation](https://github.com/commitizen/cz-cli)