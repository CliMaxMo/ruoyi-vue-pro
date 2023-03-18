# 1.函数简介

### 1.1 createApp

ue3 中的应用是通过使用 createApp 函数来创建的，语法格式如下：

```
const app = Vue.createApp({ /* 选项 */ })
```

传递给 createApp 的选项用于配置根组件。在使用 **mount()** 挂载应用时，该组件被用作渲染的起点。

一个简单的实例：

```vue
<div id="hello-vue" class="demo">
  {{ message }}
</div>
​
<script>
const HelloVueApp = {
  data() {
    return {
      message: 'Hello Vue!!'
    }
  }
}
​
Vue.createApp(HelloVueApp).mount('#hello-vue')
</script>
```

createApp 的参数是根组件（HelloVueApp），在挂载应用时，该组件是渲染的起点。

一个应用需要被挂载到一个 DOM 元素中，以上代码使用 **mount('#hello-vue')** 将 Vue 应用 HelloVueApp 挂载到 **<div id="hello-vue"></div>** 中。

### 1.2 data

**data 选项**是一个函数。Vue 在创建新组件实例的过程中调用此函数。它应该返回一个对象，然后 Vue 会通过响应性系统将其包裹起来，并以 $data 的形式存储在组件实例中。

```tsx
const app = Vue.createApp({
  data() {
    return { count: 4 }
  }
})

const vm = app.mount('#app')

document.write(vm.$data.count) // => 4
document.write("<br>")
document.write(vm.count)       // => 4
document.write("<br>")
// 修改 vm.count 的值也会更新 $data.count
vm.count = 5
document.write(vm.$data.count) // => 5
document.write("<br>")
// 反之亦然
vm.$data.count = 6
document.write(vm.count) // => 6
```

### 1.3 methods

我们可以在组件中添加方法，使用 **methods** 选项，该选项包含了所需方法的对象。

以下实例我们添加了 methods 选项，选项中包含了 **increment()** 方法：

```tsx
const app = Vue.createApp({
  data() {
    return { count: 4 }
  },
  methods: {
    increment() {
      // `this` 指向该组件实例
      this.count++
    }
  }
})

const vm = app.mount('#app')

document.write(vm.count) // => 4
document.write("<br>")
vm.increment()

document.write(vm.count) // => 5
```

### 1.4 插值

#### 1.4.1 **文本**

数据绑定最常见的形式就是使用 **{{...}}**（双大括号）的文本插值：

**{{...}}** 标签的内容将会被替代为对应组件实例中 **message** 属性的值，如果 **message** 属性的值发生了改变，**{{...}}** 标签内容也会更新。

如果不想改变标签的内容，可以通过使用 **v-once** 指令执行一次性地插值，当数据改变时，插值处的内容不会更新。

**Html**

```vue
<div id="example1" class="demo">
    <p>使用双大括号的文本插值: {{ rawHtml }}</p>
    <p>使用 v-html 指令: <span v-html="rawHtml"></span></p>
</div>
 
<script>
const RenderHtmlApp = {
  data() {
    return {
      rawHtml: '<span style="color: red">这里会显示红色！</span>'
    }
  }
}
 
Vue.createApp(RenderHtmlApp).mount('#example1')
</script>
```

#### 1.4.2 **v-bind**

HTML 属性中的值应使用 v-bind 指令。

```
<div v-bind:id="dynamicId"></div>
```

对于布尔属性，常规值为 true 或 false，如果属性值为 null 或 undefined，则该属性不会显示出来。

```
<button v-bind:disabled="isButtonDisabled">按钮</button>
```

**缩写**

```
<!-- 完整语法 -->
<a v-bind:href="url"></a>
<!-- 缩写 -->
<a :href="url"></a>
```

##### Class 与 Style 绑定

**class**：

数据绑定的一个常见需求场景是操纵元素的 CSS class 列表和内联样式。因为 `class` 和 `style` 都是 attribute，我们可以和其他 attribute 一样使用 `v-bind` 将它们和动态的字符串绑定。但是，在处理比较复杂的绑定时，通过拼接生成字符串是麻烦且易出错的。因此，Vue 专门为 `class` 和 `style` 的 `v-bind` 用法提供了特殊的功能增强。除了字符串外，表达式的值也可以是对象或数组。

```vue
<div
  class="static"
  :class="{ active: isActive, 'text-danger': hasError }"
></div>

结果
<div class="static active"></div>
```

**style**:

```JS
data() {
  return {
    activeColor: 'red',
    fontSize: 30
  }
}
<div :style="{ color: activeColor, fontSize: fontSize + 'px' }"></div>

或者
data() {
  return {
    styleObject: {
      color: 'red',
      fontSize: '13px'
    }
  }
}

<div :style="styleObject"></div>
```

绑定数组

```html
<div :style="[baseStyles, overridingStyles]"></div>
```



#### 1.4.3 **表达式**

```tsx
<div id="app">
    {{5+5}}<br>
    {{ ok ? 'YES' : 'NO' }}<br>
    {{ message.split('').reverse().join('') }}
    <div v-bind:id="'list-' + id">菜鸟教程</div>
</div>
    
<script>
const app = {
  data() {
    return {
      ok: true,
      message: 'RUNOOB!!',
      id: 1
    }
  }
}
 
Vue.createApp(app).mount('#app')
输出：
10
YES
!!BOONUR
菜鸟教程
```

#### 1.4.4 **v-if **

指令是带有 v- 前缀的特殊属性。

指令用于在表达式的值改变时，将某些行为应用到 DOM 上。如下例子：

```vue
<div id="app">
    <p v-if="seen">现在你看到我了</p>
</div>
    
<script>
const app = {
  data() {
    return {
      seen: true  /* 改为false，信息就无法显示 */
    }
  }
}
 
Vue.createApp(app).mount('#app')
</script>
```

这里， v-if 指令将根据表达式 seen 的值( true 或 false )来决定是否插入 p 元素。

#### 1.4.5 v-on

v-on 指令，它用于监听 DOM 事件

```vue
<!-- 完整语法 -->
<a v-on:click="doSomething"> ... </a>

<!-- 缩写 -->
<a @click="doSomething"> ... </a>

<!-- 动态参数的缩写 (2.6.0+) -->
<a @[event]="doSomething"> ... </a>
```

**缩写**

```vue
<!-- 完整语法 -->
<a v-on:click="doSomething"></a>
<!-- 缩写 -->
<a @click="doSomething"></a>
```

#### 1.4.6 修饰符

修饰符是以半角句号 **.** 指明的特殊后缀，用于指出一个指令应该以特殊方式绑定。例如，**.prevent** 修饰符告诉 **v-on** 指令对于触发的事件调用 **event.preventDefault()**：

```
<form v-on:submit.prevent="onSubmit"></form>
```

#### 1.4.7 v-message

```vue
<div id="app">
    <p>{{ message }}</p>
    <input v-model="message">
</div>
 
<script>
const app = {
  data() {
    return {
      message: 'Runoob!'
    }
  }
}
 
Vue.createApp(app).mount('#app')
</script>
```

#### 1.4.8 v-else-if

v-else-if 即 v-if 的 else-if 块，可以链式的使用多次：

```vue
<div id="app">
    <div v-if="type === 'A'">
         A
    </div>
    <div v-else-if="type === 'B'">
      B
    </div>
    <div v-else-if="type === 'C'">
      C
    </div>
    <div v-else>
      Not A/B/C
    </div>
</div>
    
<script>
const app = {
  data() {
    return {
      type: "C" 
    }
  }
}
 
Vue.createApp(app).mount('#app')
</script>  



<template v-if="ok">
  <h1>Title</h1>
  <p>Paragraph 1</p>
  <p>Paragraph 2</p>
</template>
```

#### 1.4.9 v-show

确认是否显示 

与if不同之处在于 `v-show` 会在 DOM 渲染中保留该元素；`v-show` 仅切换了该元素上名为 `display` 的 CSS 属性。

`v-show` 不支持在 `<template>` 元素上使用，也不能和 `v-else` 搭配使用。

```vue
<h1 v-show="ok">Hello!</h1>
```

#### 1.4.10 v-for

另外还有其它很多指令，每个都有特殊的功能。例如，v-for 指令可以绑定数组的数据来渲染一个项目列表：

```vue
<div id="app">
  <ol>
    <li v-for="site in sites">
      {{ site.text }}
    </li>
  </ol>
</div>
<script>
const app = {
  data() {
    return {
      sites: [
        { text: 'Google' },
        { text: 'Runoob' },
        { text: 'Taobao' }
      ]
    }
  }
}

Vue.createApp(app).mount('#app')
</script>
```

v-for 可以通过一个对象的属性来迭代数据：键值对

```vue
<div id="app">
  <ul>
    <li v-for="value in object">
    {{ value }}
    </li>
  </ul>
</div>
 
<script>
const app = {
  data() {
    return {
      object: {
        name: '菜鸟教程',
        url: 'http://www.runoob.com',
        slogan: '学的不仅是技术，更是梦想！'
      }
    }
  }
}
 
Vue.createApp(app).mount('#app')
</script>

菜鸟教程
http://www.runoob.com
学的不仅是技术，更是梦想！


<div id="app">
  <ul>
    <li v-for="(value, key) in object">
    {{ key }} : {{ value }}
    </li>
  </ul>
</div>
 
<script>
const app = {
  data() {
    return {
      object: {
        name: '菜鸟教程',
        url: 'http://www.runoob.com',
        slogan: '学的不仅是技术，更是梦想！'
      }
    }
  }
}
 
Vue.createApp(app).mount('#app')
</script>
name : 菜鸟教程
url : http://www.runoob.com
slogan : 学的不仅是技术，更是梦想！
```

迭代整数

```vue
<div id="app">
  <ul>
    <li v-for="n in 5">
     {{ n }}
    </li>
  </ul>
</div>

1
2
3
4
5
```

迭代过滤函数

```vue
<div id="app">
  <ul>
    <li v-for="n in evenNumbers">{{ n }}</li>
  </ul>
</div>
 
<script>
const app = {
    data() {
        return {
            numbers: [ 1, 2, 3, 4, 5 ]
	     }
    },
    computed: {
        evenNumbers() {
            return this.numbers.filter(number => number % 2 === 0)
        }
    }
}

2
4
```

### 1.5 组件

语法

```vue
app.component('my-component-name', {
  /* ... */
})
```

**my-component-name** 为组件名，**/\* ... \*/** 部分为配置选项。注册后，我们可以使用以下方式来调用组件：

```vue
<my-component-name></my-component-name>
```

例子：

```vue
<div id="app">
    <runoob></runoob>
</div>

<script>
 
// 创建一个Vue 应用
const app = Vue.createApp({})
 
// 定义一个名为 runoob 的新全局组件
app.component('runoob', {
    template: '<h1>自定义组件!</h1>'
})
 
app.mount('#app')
---------  
自定义组件!
```

例子2（叫上方法和参数）

```vue
<div id="app">
    <button-counter></button-counter>
</div>

<script>
// 创建一个Vue 应用
const app = Vue.createApp({})

// 定义一个名为 button-counter 的新全局组件
app.component('button-counter', {
  data() {
    return {
      count: 0
    }
  },
  template: `
    <button @click="count++">
      点了 {{ count }} 次！
    </button>`
})
app.mount('#app')
</script>
```

#### 全局组件

```vue
在 main.js 中 注册
Vue.component("component-a", {
  template: "<div><div>{{ msg }}</div></div>",
  data() {
    return {
      msg: "我是组件 A"
    };
  }
});
Vue.component("component-b", {
  template: "<div><div>{{ msg }}</div></div>",
  data() {
    return {
      msg: "我是组件 B"
    };
  }
});
```

#### 局部组件

```vue
<template>
  <div>
    <component-a></component-a>
    <component-b></component-b>
  </div>
</template>
<script>
import ComponentA from "@/components/CompenentA.vue";
import ComponentB from "@/components/CompenentB.vue";
export default {
  components: {
    ComponentA,
    ComponentB,
  },
};
</script>
```

实际全局应用

1. 新建 新建 src/utils/globalComponents.js, 引入全局组件

```jsx
// 全局引入

import ComponentA from "@/components/CompenentA.vue";
import ComponentB from "@/components/CompenentB.vue";

export default {
  ComponentA,
  ComponentB
};
```

2. 新建 src/utils/global.js，在 install 函数中，遍历重构组件对象

```jsx
// 引入组件
import components from "./globalComponents.js";

const globals = {
  install: function(Vue, option) {
    Object.keys(components).forEach(key => {
      // Object.keys(components) //返回数组，数组里面的每一项是对象的属性名称
      Vue.component(key, components[key]);
    });
  }
};

export default globals;
```

3. 在 main.js 中，引入并 use

```css
import globals from "@/utils/global.js";
Vue.use(globals);
```

4. 在组件中使用

```xml
<template>
  <div>
    <component-a></component-a>
    <component-b></component-b>
  </div>
</template>
<script>
export default {};
</script>
```

### 1.6 nextTick

https://www.jianshu.com/p/a7550c0e164f

当你更改响应式状态后，DOM 会自动更新。然而，你得注意 DOM 的更新并不是同步的。相反，Vue 将缓冲它们直到更新周期的 “下个时机” 以确保无论你进行了多少次状态更改，每个组件都只更新一次。

当有值赋值时，DOM还未更新，在nextTick里进行操作就是当下一次DOM更新数据后进行调用

### 1.7 计算属性

**computed**: 若我们将同样的函数定义为一个方法而不是计算属性，两种方式在结果上确实是完全相同的，然而，不同之处在于**计算属性值会基于其响应式依赖被缓存**。一个计算属性仅会在其响应式依赖更新时才重新计算。这意味着只要 `author.books` 不改变，无论多少次访问 `publishedBooksMessage` 都会立即返回先前的计算结果，而不用重复执行 getter 函数。

**methods**: 相比之下，方法调用**总是**会在重渲染发生时再次执行函数

```vue
computed: {
    // 一个计算属性的 getter
    publishedBooksMessage() {
      // `this` 指向当前组件实例
      return this.author.books.length > 0 ? 'Yes' : 'No'
    }
}
methods: {
  calculateBooksMessage() {
    return this.author.books.length > 0 ? 'Yes' : 'No'
  }
}
```

computed默认是可读属性，但是当你需要可写属性时，可以通过get和set来实现

```vue
export default {
  data() {
    return {
      firstName: 'John',
      lastName: 'Doe'
    }
  },
  computed: {
    fullName: {
      // getter
      get() {
        return this.firstName + ' ' + this.lastName
      },
      // setter
      set(newValue) {
        // 注意：我们这里使用的是解构赋值语法
        [this.firstName, this.lastName] = newValue.split(' ')
      }
    }
  }
}
```

