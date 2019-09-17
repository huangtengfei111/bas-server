package app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.scijava.parse.ExpressionParser;
import org.scijava.parse.Operator;
import org.scijava.parse.Operators;
import org.scijava.parse.Variable;

import com.google.common.collect.Sets;

public class Test {

  public static void main(String[] args) throws Exception {
    Map<String, Set> map = new HashMap();

    Set<String> t1 = new HashSet<>();
    t1.add("a");
    t1.add("b");
    map.put("t1", t1);

    Set<String> t2 = new HashSet<>();
    t2.add("c");
    t2.add("d");
    map.put("t2", t2);

    Set<String> t3 = new HashSet<>();
    t3.add("a");
    t3.add("c");
    map.put("t3", t3);

    Set<String> t4 = new HashSet<>();
    t4.add("a");
    t4.add("b");
    t4.add("c");
    map.put("t4", t4);

    LinkedList<Object> queue = new ExpressionParser().parsePostfix("(t1 + t2 - (t3 & t4))");
    System.out.println(queue);
    LinkedList<Set> stack = new LinkedList<>();
    for (Object o : queue) {
      if (o instanceof Variable) {
        Variable var = (Variable) o;
        Set s = map.get(var.getToken());
        stack.push(s);
      }
      if (o instanceof Operator) {
        Operator op = (Operator) o;
        if (op.equals(Operators.ADD)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.union(v1, v2);
          stack.push(v);
        } else if (op.equals(Operators.SUB)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.difference(v1, v2);
          stack.push(v);
        } else if (op.equals(Operators.BITWISE_AND)) {
          Set v2 = stack.pop();
          Set v1 = stack.pop();
          Set v = Sets.intersection(v1, v2);
          stack.push(v);
        }
      }
    }
    System.out.println(stack.pop());
    // SyntaxTree tree = new ExpressionParser().parseTree("(t1 + t2 - (t3 & t4))");
    // System.out.println(tree);
  }

}

