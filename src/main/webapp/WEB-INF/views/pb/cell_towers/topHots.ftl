[<#if listMap?has_content>
  <#list listMap as item>    
       <#list item?keys as key>        
          <#assign lat="${item['lat']!}">
          <#assign lng="${item['lng']!}">
          <#assign count="${item['count']!}">       
       </#list> 
       {
          "coord": [${lat}, ${lng}], "elevation": ${count}
       }<#sep>,</#sep>   
  </#list>
</#if>
]




























