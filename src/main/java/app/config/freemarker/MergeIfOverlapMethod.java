package app.config.freemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class MergeIfOverlapMethod implements TemplateMethodModelEx {

  @Override
  public Object exec(List args) throws TemplateModelException {
    Object l = args.get(0);
    if (l instanceof SimpleSequence) {
      SimpleSequence l0 = (SimpleSequence) l;
      List merged = new ArrayList();
      List<Integer> takenIndex = new ArrayList<>();
      for (int i = 0; i < l0.size() && !takenIndex.contains(i); i++) {
        Object o = l0.get(i);
        if (o instanceof SimpleSequence) {
          SimpleSequence l1 = (SimpleSequence) o;
          Set s1 = simpleSequence2Set(l1);
          for (int j = i + 1; j < l0.size() && !takenIndex.contains(j); j++) {
            SimpleSequence l2 = (SimpleSequence) l0.get(j);
            Set s2 = simpleSequence2Set(l2);
            if (Sets.intersection(s1, s2).size() > 0) {
              takenIndex.add(j);
              s1 = Sets.union(s1, s2);
            }
          }
          merged.add(s1);
        } else {
          merged = args;
        }
      }
      return merged;
    } else {
      return args;
    }
  }

  private Set simpleSequence2Set(SimpleSequence seq)
      throws TemplateModelException {
    Set<String> set = new HashSet();
    for (int i = 0; i < seq.size(); i++) {
      set.add(seq.get(i).toString());
    }
    return set;
  }
  public static void main(String[] args) throws TemplateModelException {
    Collection<Set<String>> l = new ArrayList<>();
    Set<String> l1 = new HashSet<>();
    Set<String> l2 = new HashSet<>();
    Set<String> l3 = new HashSet<>();
    
    l1.add("a");
    l1.add("b");
    l1.add("c");
    l1.add("d");

    l2.add("e");
    l2.add("f");
    l2.add("g");

    l3.add("z");
    l3.add("g");
    l3.add("y");

    l.add(l1);
    l.add(l2);
    l.add(l3);
    
    MergeIfOverlapMethod m = new MergeIfOverlapMethod();
    List a = new ArrayList();
    Object ll = l;
    a.add(ll);
    System.out.println(a);
    Object o = m.exec(a);
    System.out.println(o);
  }
}
