[
<#if linkedHashMap?has_content>
  <#if useKey??>
    <#list linkedHashMap as lhmK, lhmV>
     <#if valueType == 'listMap'>
       {"${lhmK!}" : [
           <#list lhmV as lm>
             {
            <#list lm as k, v>
               "${k!""}":"${v!""}"<#sep>,</#sep> 
            </#list>
             }<#sep>,</#sep>    
           </#list>
       ]}<#sep>,</#sep>     
     <#else>
       {"${lhmK!}" : [
          {
           <#list lhmV as k,v >
               "${k!""}":"${v!""}"<#sep>,</#sep> 
           </#list>
          } 
       ]}<#sep>,</#sep>
     </#if> 
    </#list>
  <#else>
    <#list linkedHashMap as lhmK, lhmV>
     <#if valueType == 'listMap'>
       [
         <#list lhmV as lm>
           {
          <#list lm as k, v>
             "${k!}":"${v!}"<#sep>,</#sep> 
          </#list>
           }<#sep>,</#sep>    
         </#list>
       ]<#sep>,</#sep>           
     <#else>
       {
         <#list lhmV as k,v >
             "${k!}":"${v!}"<#sep>,</#sep> 
         </#list>
       }<#sep>,</#sep>
     </#if> 
    </#list> 
  </#if>
</#if>
]