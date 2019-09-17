{
  <#if items?has_content>
   "codes" :  [
              <#list items as item >
                "${item}"<#sep>,</#sep>
              </#list>
            ]
  </#if>
}