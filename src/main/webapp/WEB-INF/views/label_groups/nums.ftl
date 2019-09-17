{
  <#if items?has_content>
   "nums" :  [
              <#list items as item >
                "${item}"<#sep>,</#sep>
              </#list>
            ]
  </#if>
}