package cn.iocoder.yudao.module.mes.service;

import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @title: Spel
 * @Author cmw
 * @Date: 2023/2/27 14:48
 * @describe
 */
public class Spel {
    @Test
    public void First(){
        String s=" 'Hello ,#{#user}'.bytes.length ";

       ExpressionParser parser=new SpelExpressionParser();

       EvaluationContext evaluationContext =new StandardEvaluationContext();
       evaluationContext.setVariable("user","world");
//       Expression expression=parser.parseExpression(s,new TemplateParserContext());
//       String rs= (String) expression.getValue(evaluationContext);

       Expression expression=parser.parseExpression(s);

       int rs= (int) expression.getValue();
       System.out.println(rs);

    }

    @Test
    public  void Second(){

        String expression = "Authenticated";
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        User user = new User();

        ctx.setRootObject(user);
        Boolean value = exp.getValue(ctx, Boolean.class);
        System.out.println("value = " + value);

    }

    public class User {
        private Integer id;
        private String username;
        private String address;
        //省略 getter/setter
        public   boolean isAuthenticated(){
            return  false;
        }
    }


}
