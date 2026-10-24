import reactNativeConfig from '@react-native/eslint-config/flat';
import prettierRecommended from 'eslint-plugin-prettier/recommended';

// This package has no Flow code, and the Flow plugin pinned by
// @react-native/eslint-config does not run on ESLint 9.
const withoutFlowRules = reactNativeConfig.map((config) =>
  config.rules
    ? {
        ...config,
        rules: Object.fromEntries(
          Object.entries(config.rules).filter(
            ([name]) => !name.startsWith('ft-flow/')
          )
        ),
      }
    : config
);

export default [
  { ignores: ['node_modules/', 'lib/', 'docs/'] },
  ...withoutFlowRules,
  prettierRecommended,
];
