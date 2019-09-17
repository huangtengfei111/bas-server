<#if licenseContent?has_content>
<@render partial="/admin/license/license" />
<#else>
{ "host_id": "${(profile.hostId)!}"}
</#if>
