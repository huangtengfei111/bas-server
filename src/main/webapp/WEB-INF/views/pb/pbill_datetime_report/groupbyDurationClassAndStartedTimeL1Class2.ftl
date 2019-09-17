[
<#if resultMap?has_content>
    <#list resultMap as key,value>
      {
      "date": "${key}",
      <#list value as item >
      
          <#list item? keys as key>
            <#if key?contains("stl")>
              "${item[key]}":${item["c"]}, 
              <#elseif key == "c">
              <#else>
              "${key}":"${item[key]}" <#sep>,</#sep> 
            </#if>
           
        </#list>
      </#list>
      }<#sep>,</#sep> 
    </#list>
  
</#if>
]
