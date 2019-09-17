[
<#if linkedHashMap?has_content>
    <#list linkedHashMap as lhmK, lhmV>
     <#if valueType == 'listMap'>
       [
         <#assign str = "">
        {
         <#list lhmV as lm>
           <#if lm_index = 0>
            <#list lm as k, v>
             "${k!""}":"${v!""}",
            </#list>
           <#else>
            <#list lm as k, v>
             <#if k == "started_time">
              <#assign str1 = "<span class='time'>" + v + "</span>">
             <#elseif !comboKey?seq_contains(k)>
              <#assign str2 = k + v + " ">
             </#if>   
            </#list>
            <#assign str = str + str1 + str2>
           </#if>
         </#list>
         "memo" : "${str!""}"
        }  
       ]<#sep>,</#sep>           
     </#if> 
    </#list>      
</#if>
]
