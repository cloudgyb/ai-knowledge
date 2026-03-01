---
name: api-doc-generator
description: 为项目生成API文档
allowed-tools: "Read,Write,Bash"
version: 1.0.0
---

# api-doc-generator

为项目生成API文档

## API 文档生成

- 在项目的根目录创建 doc 文件夹，并创建 api.md 文件，用于存放接口文档
- 审查代码，找到所有 Spring MVC 的控制器类，分析接口方法，生成服务 openapi 规范的接口文档