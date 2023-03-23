## 二、Pinia的使用

### 一、安装与引入

安装

```js
npm install pinia   //"pinia": "^2.0.13" 
```

项目中引入Pinia

```js
import {createPinia} from 'pinia'          //main.js导入
createApp(App).use(createPinia()).mount('#app')  //createPinia()得加上括号
```

### 二、重点函数介绍

### 1)defineStore定义容器

参数1：是对仓库的命名，名称必须具备唯一性；

参数2：配置的选项对象，即state、getters、actions，其中state的写法必须是函数，为了避免在服务端交叉请求导致的状态数据污染，而且必须是箭头函数，为了更好的TS类型推导。

```js
import {defineStore} from "pinia";
export const useMainStore =  defineStore('main',{
  state:()=>{return{}},
  getters:{},
  actions:{}
})
```

### 2)storeToRefs()函数读取state

使用storeToRefs()将state中的状态解构出来，方便在视图中使用，storeToRefs函数可将普通数据变成响应式数据。

```js
import {storeToRefs} from 'pinia'
const {count,name,arr,count10} = storeToRefs(mainStore)
```

### 3)patch 函数修改state

当需要修改state中多个数据时用$patch ，$patch函数会批量更新，此时需要传入state参数。

```js
mainStore.$patch(state=>{      
          state.count++
          state.name += '~~'
          state.arr.push(5)
})
```

### 4)Action通过函数更改state

在store/index.js中添加changeState方法，在组件中用store调用。

注：定义actions时不要使用箭头函数，因为箭头函数绑定外部this。使用容器中的state 时，action通过this操作；此外，还可以通过$patch修改state的数据。

```js
actions:{  
  changeState(num,str){
      this.count += num     //action通过this操作state的数据
      this.name += str
      this.arr.push(5)
      this.$patch({})
      this.$patch(state=>{})
  }
}
```

### 5)Getters类似Vuex的计算属性

在store/index.js的Getters函数中添加count10()方法，在组件中用store调用，getters函数接收state参数。

注：若组件中使用ts，getters使用this时，必须指定类型，否则会导致推导错误。

```js
getters:{
      count10(state){
       return state.count + 10
       }
      countOther():number{
          return this.count += 12
      }
},
```

### 三、总体代码演示

store/index.js文件

```js
import {defineStore} from "pinia";
//参数1：定义一个仓库的唯一id名，Pinia会将所有的容器挂载到根容器；参数2：选项对象
export const useMainStore =  defineStore('main',{
  state:()=>{//state必须是函数，是避免在服务端渲染时的交叉请求导致的状态数据污染；必须是箭头函数，为了更好的TS类型推导
    return{
      count:10,
      name:'wl',
      arr:[1,2,3],
    }
  },
    getters:{
        //函数接受一个可选参数:state
    count10(state){return state.count + 10}
        /*count10():number{   //在TS文件下，getters使用了this,则必须指定类型，否则会导致推导错误
          return this.count += 12
        }*/
    },
    actions:{
      changeState(num,str){   //不要使用箭头函数定义action,因为箭头函数绑定外部this
          this.count += num     
          this.name += str
          this.arr.push(5)          
          // this.$patch({})或this.$patch(state=>{})    //还可通过$patch修改state的数据
      }
    }
})
```

Vue组件中的使用

```js
<template>
  <h2>数量：{{mainStore.count}}---{{count}}</h2>
  <h2>姓名：{{mainStore.name}}---{{name}}</h2>
  <h2>arr：{{mainStore.arr}}---{{arr}}</h2>
  <h2>getter的使用：{{mainStore.count10}}---{{count10}}</h2>
  <button @click="changeNum">修改数量</button>
</template>
<script >
    import {useMainStore} from "./store/index.js"
    import {storeToRefs} from 'pinia'
    import {toRefs} from "vue";
export default {
  name: 'App',
  setup(){
    const mainStore = useMainStore()
      const {count,name,arr,count10} = storeToRefs(mainStore)//使用storeToRefs函数将state里的数据解构出来实现响应式
      function changeNum() {
          mainStore.count++            //法1
          mainStore.$patch({           //法2：修改多个数据，用$patch
              count:mainStore.count + 1,
              name:mainStore.name + '!',
              arr:[...mainStore.arr,4]
          })         
          mainStore.$patch(state=>{          //法3：$patch函数会批量更新
              state.count++
              state.name += '~~'
              state.arr.push(5)
          })
          mainStore.changeState(10,'hello')   //法4：逻辑比较多时使用actions
      }
      return{count,name,arr,count10,mainStore,changeNum}
  }
}
</script>
```

### 四、DevTools调试工具

使用开发工具，能跟踪动作、突变的时间表；商店出现在使用它们的组件中；时间旅行和更容易的调试。

![img](https://pic3.zhimg.com/80/v2-b1226d06f0fd6b9639276ec4521ef79a_720w.webp)