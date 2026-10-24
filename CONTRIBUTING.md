# Contributing

Contributions are always welcome, no matter how large or small!

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project. Before contributing, please read the [code of conduct](./CODE_OF_CONDUCT.md).

## Development workflow

Install dependencies with npm. This also downloads the iOS MEPSDK framework into `ios/Frameworks`:

```sh
npm install
```

Make sure your code passes TypeScript, ESLint and the unit tests:

```sh
npm run typescript
npm run lint
npm test
```

To fix formatting errors, run `npx eslint . --fix`.

The native module is a Turbo Module. Its interface is defined in `src/NativeMoxoModule.ts`, and Codegen generates the native specs from it when an app builds. If you change that file, update `ios/MoxoModule.mm`, `ios/MoxoModuleImpl.swift` and `android/src/main/java/com/moxomodule/MoxoModuleModule.kt` to match.

To test native changes, install the package into a React Native app (for example with `npm pack` and `npm install <path-to-tgz>`) and build it for both platforms.

### Commit message convention

We follow the [conventional commits specification](https://www.conventionalcommits.org/en) for our commit messages:

- `fix`: bug fixes, e.g. fix crash due to deprecated method.
- `feat`: new features, e.g. add new method to the module.
- `refactor`: code refactor, e.g. migrate from class components to hooks.
- `docs`: changes into documentation, e.g. add usage example for the module..
- `test`: adding or updating tests, e.g. add integration tests using detox.
- `chore`: tooling changes, e.g. change CI config.

### Linting and tests

[ESLint](https://eslint.org/), [Prettier](https://prettier.io/), [TypeScript](https://www.typescriptlang.org/)

We use [TypeScript](https://www.typescriptlang.org/) for type checking, [ESLint](https://eslint.org/) with [Prettier](https://prettier.io/) for linting and formatting the code, and [Jest](https://jestjs.io/) for testing.

### Publishing to npm

We use [release-it](https://github.com/release-it/release-it) to make it easier to publish new versions. It handles common tasks like bumping version based on semver, creating tags and releases etc.

To publish new versions, run the following:

```sh
npx release-it
```

### Scripts

The `package.json` file contains various scripts for common tasks:

- `npm run typescript`: type-check files with TypeScript.
- `npm run lint`: lint files with ESLint.
- `npm test`: run unit tests with Jest.
- `npm run doc`: generate the API docs in `docs/`.
- `npm run clean`: remove build output.

### Sending a pull request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

When you're sending a pull request:

- Prefer small pull requests focused on one change.
- Verify that linters and tests are passing.
- Review the documentation to make sure it looks good.
- Follow the pull request template when opening a pull request.
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.
