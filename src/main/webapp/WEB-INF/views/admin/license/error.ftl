<@content for="ext">
  <#if versionInfo?has_content>
    {
      <#list versionInfo?keys as k>
        "${k}": "${versionInfo[k]}"<#sep>, </#sep>
      </#list>
    }
  </#if>
</@content>
[]