# Project Rules

## Git Commit Rules

1. **每次代码改动都必须提交到 Git**
   - 任何代码修改、新增、删除完成后，必须立即执行 `git add` 和 `git commit`。
   - 提交信息（commit message）应清晰描述本次改动的内容。

2. **单元测试通过后才能推送到 GitHub**
   - 在推送代码到 GitHub 之前，必须先运行项目的单元测试。
   - 只有当所有单元测试全部通过后，才能执行 `git push`。
   - 远程仓库地址：`https://github.com/Vesperi174/BusinessLedgerSystem.git`

## 工作流程

```
代码改动 → git add → git commit → 运行单元测试 → 测试通过 → git push
```

如果单元测试失败，必须先修复问题，重新提交，确保测试通过后再推送。