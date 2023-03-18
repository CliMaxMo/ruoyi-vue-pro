## 1. API详解

1. **@ControllerAdvice**

​		声明一些全局性的东西

2. **@RestControllerAdvice**

​		@RestControllerAdvice 里面也是包含了@ControllerAdvice 、@ResponseBody

3. **@ExceptionHandler**

​		 全局捕捉异常注解

4. **@ResponseBodyAdvice**

​		　ResponseBodyAdvice 接口是在 Controller 执行 return 之后，在 response 返回给客户端之前，执行的对 response 的一些处理，可以实现对 response 数据的一些统一封装或者加密等操作。

　　该接口一共有两个方法：

（1）supports —— 判断是否要执行beforeBodyWrite方法，true为执行，false不执行 —— 通过supports方法，我们可以选择哪些类或哪些方法要对response进行处理，其余的则不处理。

（2）beforeBodyWrite —— 对 response 处理的具体执行方法。

## 2.使用详解

我们都知道做项目一般都会有全局异常统一处理的类，那么这个类在Spring中可以用@ControllerAdvice来实现，费话不多说，先看代码：

```
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;import java.util.HashMap;
import java.util.Map;/**
 * rest controller 异常捕捉
 *
 * @author sam
 * @since 2017/7/17
 */
@ControllerAdvice
public class MyControllerAdvice {    /**
     * 全局异常捕捉处理
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map errorHandler(Exception ex) {
        Map map = new HashMap();
        map.put("code", 400);
        map.put("msg", ex.getMessage());
        return map;
    }    /**
     * 全局捕捉自定义异常（凡是抛出MyException的异常都会走这里）
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = MyException.class)
    public Map myErrExceptionHandler(MyException ex) {
        Map map = new HashMap();
        map.put("code", 400);
        map.put("msg", ex.getMsg());
        return map;
    }
}
```