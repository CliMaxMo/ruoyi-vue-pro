

## 1. API详解

### 1.1 具体类

####  1.1.1 ExpressionParser

表达式解析器接口

包含了`(Expression) parseExpression(String)`, `(Expression) parseExpression(String, ParserContext)`两个接口方法

#### 1.1.2 ParserContext

解析器上下文接口，主要是对解析器Token的抽象类，包含3个方法：`getExpressionPrefix`,`getExpressionSuffix`和`isTemplate`，就是表示表达式从什么符号开始什么符号结束，是否是作为模板（包含字面量和表达式）解析。一般保持默认。

####  1.1.3 Expression

表达式的抽象，是经过解析后的字符串表达式的形式表示。通过`expressionInstance.getValue`方法，可以获取表示式的值。也可以通过调用`getValue(EvaluationContext)`，从评估（evaluation)上下文中获取表达式对于当前上下文的值

#### 1.1.4 EvaluationContext

估值上下文接口，只有一个setter方法：`setVariable(String, Object)`，通过调用该方法，可以为evaluation提供上下文变量

#### 1.1.5 ParserContext 

解析器上下文

```
public interface ParserContext {
    ParserContext TEMPLATE_EXPRESSION = new ParserContext() {
        public boolean isTemplate() {
            return true;
        }

        public String getExpressionPrefix() {
            return "#{";
        }

        public String getExpressionSuffix() {
            return "}";
        }
    };

    boolean isTemplate();
	//获得表达式前缀
    String getExpressionPrefix();
	//获得表达式后缀
    String getExpressionSuffix();
}
```

#### 1.1.6  TemplateParserContext

```java
public class TemplateParserContext implements ParserContext {
    private final String expressionPrefix;
    private final String expressionSuffix;
	//如TemplateParserContext 前缀就是#{,后缀就是}
    public TemplateParserContext() {
        this("#{", "}");
    }

    public TemplateParserContext(String expressionPrefix, String expressionSuffix) {
        this.expressionPrefix = expressionPrefix;
        this.expressionSuffix = expressionSuffix;
    }

    public final boolean isTemplate() {
        return true;
    }

    public final String getExpressionPrefix() {
        return this.expressionPrefix;
    }

    public final String getExpressionSuffix() {
        return this.expressionSuffix;
    }
}
```

### 1.2 具体例子

基础用法

```dart
public static void main(String[] args) {        
    String greetingExp = "Hello, #{ #user }";                             （1）     
    ExpressionParser parser = new SpelExpressionParser();           （2）
    EvaluationContext context = new StandardEvaluationContext();        
    context.setVariable("user", "Gangyou");        (3)

    Expression expression = parser.parseExpression(greetingExp, 
        new TemplateParserContext());     (4)
    System.out.println(expression.getValue(context, String.class)); (5)
}
```

其中``EvaluationContext``就是提供上下文环境的

举个例子比如

```java
//对象
public class User {
    private Integer id;
    private String username;
    private String address;
    //省略 getter/setter
}

//通过设置上下文环境 设置user对象。这样子赋值
String expression = "#user.username";
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression(expression);
StandardEvaluationContext ctx = new StandardEvaluationContext();
User user = new User();
user.setAddress("广州");
user.setUsername("javaboy");
user.setId(99);
ctx.setVariable("user", user);
String value = exp.getValue(ctx, String.class);
System.out.println("value = " + value);

```

如果我们将 user 对象设置为 rootObject，那么表达式中就不需要 user 了，如下：

```java
String expression = "username";
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression(expression);
StandardEvaluationContext ctx = new StandardEvaluationContext();
User user = new User();
user.setAddress("广州");
user.setUsername("javaboy");
user.setId(99);
ctx.setRootObject(user);
String value = exp.getValue(ctx, String.class);
System.out.println("value = " + value);
```

表达式就一个 username 字符串，将来执行的时候，会自动从 user 中找到 username 的值并返回。

当然表达式也可以是方法，例如我在 User 类中添加如下两个方法：

```java
public String sayHello(Integer age) {
    return "hello " + username + ";age=" + age;
}
public String sayHello() {
    return "hello " + username;
}
```

我们就可以通过表达式调用这两个方法，如下：

调用有参的 sayHello：

```java
String expression = "sayHello(99)";
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression(expression);
StandardEvaluationContext ctx = new StandardEvaluationContext();
User user = new User();
user.setAddress("广州");
user.setUsername("javaboy");
user.setId(99);
ctx.setRootObject(user);
String value = exp.getValue(ctx, String.class);
System.out.println("value = " + value);
```

就直接写方法名然后执行就行了。

调用无参的 sayHello：

```java
String expression = "sayHello";
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression(expression);
StandardEvaluationContext ctx = new StandardEvaluationContext();
User user = new User();
user.setAddress("广州");
user.setUsername("javaboy");
user.setId(99);
ctx.setRootObject(user);
String value = exp.getValue(ctx, String.class);
System.out.println("value = " + value);
```

这些就都好懂了。

**甚至，我们的表达式也可以涉及到 Spring 中的一个 Bean，例如我们向 Spring 中注册如下 Bean：**

```java
@Service("us")
public class UserService {
    public String sayHello(String name) {
        return "hello " + name;
    }
}
```

然后通过 SpEL 表达式来调用这个名为 us 的 bean 中的 sayHello 方法，如下：

```java
@Autowired
BeanFactory beanFactory;
@Test
void contextLoads() {
    String expression = "@us.sayHello('javaboy')";
    ExpressionParser parser = new SpelExpressionParser();
    Expression exp = parser.parseExpression(expression);
    StandardEvaluationContext ctx = new StandardEvaluationContext();
    ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));
    String value = exp.getValue(ctx, String.class);
    System.out.println("value = " + value);
}
```

给配置的上下文环境设置一个 bean 解析器，这个 bean 解析器会自动跟进名字从 Spring 容器中找打响应的 bean 并执行对应的方法。

## 2.源码解析

### 2.1 没有模板的源码解析 TODO 待完善

``parser.parseExpression`` 首先进入``TemplateAwareExpressionParser``的``parseExpression``方法

```java
//判断传入的解析上下文是否不为空且是否为模板
//并且是则进入parseTemplate方法，不是则进入doParseExpression方法
public Expression parseExpression(String expressionString, @Nullable ParserContext context) throws ParseException {
    return context != null && context.isTemplate() ? this.parseTemplate(expressionString, context) : this.doParseExpression(expressionString, context);
}
```

``parseTemplate``

```java
private Expression parseTemplate(String expressionString, ParserContext context) throws ParseException {
    if (expressionString.isEmpty()) {
        return new LiteralExpression("");
    } else {
        Expression[] expressions = this.parseExpressions(expressionString, context);
        //等分割结束后再创建CompositeStringExpression
        return (Expression)(expressions.length == 1 ? expressions[0] : new CompositeStringExpression(expressionString, expressions));
    }
}
```

``parseExpressions``

```
private Expression[] parseExpressions(String expressionString, ParserContext context) throws ParseException {
    List<Expression> expressions = new ArrayList();
    String prefix = context.getExpressionPrefix();
    String suffix = context.getExpressionSuffix();
    int startIdx = 0;
	//循环分割，根据解析表达式模板来分割 如‘hello,#{#user}123’ 就分割成数组 1.hello，  2.user  3.123
    while(startIdx < expressionString.length()) {
        int prefixIndex = expressionString.indexOf(prefix, startIdx);
        if (prefixIndex >= startIdx) {
        	//分割前缀之前的 创建（字面）LiteralExpression
            if (prefixIndex > startIdx) {
                expressions.add(new LiteralExpression(expressionString.substring(startIdx, prefixIndex)));
            }

            int afterPrefixIndex = prefixIndex + prefix.length();
            //后去后缀位置
            int suffixIndex = this.skipToCorrectEndSuffix(suffix, expressionString, afterPrefixIndex);
            if (suffixIndex == -1) {
                throw new ParseException(expressionString, prefixIndex, "No ending suffix '" + suffix + "' for expression starting at character " + prefixIndex + ": " + expressionString.substring(prefixIndex));
            }
			//如果后缀位置和前缀位置相同则抛出异常
            if (suffixIndex == afterPrefixIndex) {
                throw new ParseException(expressionString, prefixIndex, "No expression defined within delimiter '" + prefix + suffix + "' at character " + prefixIndex);
            }
			//获取前缀和后缀之前的字符
            String expr = expressionString.substring(prefixIndex + prefix.length(), suffixIndex);
            expr = expr.trim();
            if (expr.isEmpty()) {
                throw new ParseException(expressionString, prefixIndex, "No expression defined within delimiter '" + prefix + suffix + "' at character " + prefixIndex);
            }
			调用doParseExpression 创建SpelExpression
            expressions.add(this.doParseExpression(expr, context));
            startIdx = suffixIndex + suffix.length();
        } else {
            expressions.add(new LiteralExpression(expressionString.substring(startIdx)));
            startIdx = expressionString.length();
        }
    }

    return (Expression[])expressions.toArray(new Expression[0]);
}
```

```
PropertyOrFieldReference readProperty读取参数并执行
```