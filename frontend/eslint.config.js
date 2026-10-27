import js from "@eslint/js";
import tseslint from "typescript-eslint";
import hooks from "eslint-plugin-react-hooks";
import prettier from "eslint-config-prettier";

export default tseslint.config(
  {
    ignores: [
      "dist/**",
      "node_modules/**",
      "test-results/**",
      "playwright-report/**",
      ".test-output/**",
      "postman/mocks/**",
    ],
  },
  {
    files: ["src/**/*.{ts,tsx}", "e2e/**/*.ts", "integration/**/*.ts", "smoke/**/*.ts", "*.config.ts"],
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    plugins: { "react-hooks": hooks },
    rules: { ...hooks.configs.recommended.rules },
  },
  {
    files: ["src/**/*.{ts,tsx}"],
    ignores: ["src/app/routes.tsx", "**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-syntax": [
        "error",
        {
          selector:
            "ImportDeclaration[source.value='react-router-dom'] ImportSpecifier[imported.name=/^(Route|Routes|useRoutes|createBrowserRouter|createHashRouter)$/]",
          message: "Khai báo route chỉ nằm trong src/app/routes.tsx.",
        },
      ],
    },
  },
  {
    files: ["src/components/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/app/**", "@/features/**"],
              message: "UI dùng chung không phụ thuộc layout hoặc nghiệp vụ.",
            },
          ],
        },
      ],
    },
  },
  {
    files: ["src/api/**/*.{ts,tsx}", "src/lib/**/*.{ts,tsx}"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/app/**", "@/features/**", "@/components/**"],
              message: "Hạ tầng dùng chung không phụ thuộc màn hình hoặc nghiệp vụ.",
            },
          ],
        },
      ],
    },
  },
  {
    files: ["src/features/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [{ group: ["@/app/**"], message: "app ghép các module; module không import ngược app." }],
        },
      ],
    },
  },
  {
    files: ["src/features/**/*.{ts,tsx}"],
    ignores: ["src/features/auth/**", "**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            { group: ["@/app/**"], message: "app ghép các module; module không import ngược app." },
            {
              group: ["@/features/auth/*"],
              message: "Feature khác dùng public API @/features/auth.",
            },
          ],
        },
      ],
    },
  },
  {
    files: ["src/features/auth/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          paths: [
            {
              name: "@/features/auth",
              message: "File nội bộ Auth import trực tiếp file cùng module.",
            },
          ],
          patterns: [{ group: ["@/app/**"], message: "app ghép các module; module không import ngược app." }],
        },
      ],
    },
  },
  {
    files: ["src/features/admin/transport/shared/**/*.{ts,tsx}"],
    ignores: ["**/*.test.ts", "**/*.test.tsx"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            { group: ["@/app/**"], message: "Transport dùng chung không phụ thuộc app." },
            {
              group: [
                "@/features/admin/transport/*/api",
                "@/features/admin/transport/*/module",
                "@/features/admin/transport/*/schema",
                "@/features/admin/transport/*/pages/**",
                "../../*/api",
                "../../*/module",
                "../../*/schema",
                "../../*/pages/**",
              ],
              message: "Truyền API và cấu hình vào phần dùng chung từ module nghiệp vụ.",
            },
            {
              group: ["@/features/auth/*"],
              message: "Feature khác dùng public API @/features/auth.",
            },
          ],
        },
      ],
    },
  },
  {
    files: ["src/app/layouts/**/*.{ts,tsx}"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: [
                "@/features/admin/transport/*/module",
                "@/features/admin/transport/*/schema",
                "@/features/admin/transport/shared/pages/**",
              ],
              message: "Layout chỉ dùng thông tin điều hướng; trang và schema được tải qua route.",
            },
          ],
        },
      ],
    },
  },
  prettier
);
