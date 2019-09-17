<@content for="ext">
  <#if versionInfo?has_content>
    {
      <#list versionInfo?keys as k>
        "${k}": "${versionInfo[k]}"<#sep>, </#sep>
      </#list>
    }
  </#if>
</@content>
{
<#if licenseContent?has_content>
    "subject" : "${(licenseContent.subject)!}",
    "not_before" : "${(licenseContent.notBefore?datetime)!}",
    "not_after" : "${(licenseContent.notAfter?datetime)!}",
    "plan" : "${(licenseContent.extra.plan)!}",
    "host_id" : "${(licenseContent.extra.hostId)!}",
    "holder": "${(licenseContent.holder)!}",
    "issuer" : "${(licenseContent.issuer)!}",
    "acct_limit" : "${(licenseContent.extra.acctLimit)!}"
</#if>

}