[
<#if linkedHashMap?has_content>
    <#list linkedHashMap as lhmK, lhmV>
     <#if valueType == 'listMap'>
       [    
         <#if lhmK?has_content>
          {
           <#assign numConnection = getNumConnection(caseId,lhmK)!>
               "num_connection" : ${numConnection}
          },  
         </#if>
         <#list lhmV as lm>
           {
          <#list lm as k, v>
             "${k!""}":"${v!""}"<#sep>,</#sep> 
          </#list>
           }<#sep>,</#sep>    
         </#list>
       ]<#sep>,</#sep>           
     </#if> 
    </#list>      
</#if>
]
