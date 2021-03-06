[
<#if linkedHashMap?has_content>
    <#list linkedHashMap as lhmK, lhmV>
     <#if valueType == 'listMap'>
       [
           <#if lhmK?has_content>
            {
             <#assign cellTower = getCellTower(lhmK)!>
             "owner_ct_addr" : "${cellTower.addr!}",
              "coord" : [
                <#if (cellTower.lng)?has_content>
                ${cellTower.basCoord[0]}, ${cellTower.basCoord[1]}
                </#if>
              ]
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
