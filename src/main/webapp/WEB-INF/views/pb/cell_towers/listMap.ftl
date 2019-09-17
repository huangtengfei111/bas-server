[<#if result?has_content>
    <#list result as item>
        {
          <#list item?keys as key>
            "${key}": "${item[key]!}"<#sep>,</#sep>
          </#list>
        }<#sep>,</#sep>
    </#list>
</#if>
]