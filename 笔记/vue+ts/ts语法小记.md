ts运行

1. 编译，生成一个编译好的文件：hello.js

```
tsc hello.ts
```

2. 运行js文件

```
node hello.js
```

## 1. import 和import type

```tsx
**import** { doThing, **Options** } **from** "./foo.js";
其中Options是类型所以在生成js代码中自动删除掉它的导入（import elision的功能实现）
删除后变成了import { doThing } from "./foo.js";
```

所以当只使用import的时候他会造成一些问题

所以就就有了``import type``,使用`import type` 和`export type`导入和导出的类型只能在类型上下文中使用, 不能作为一个值来使用.

还有很多其他内容，查看https://juejin.cn/post/7111203210542448671

