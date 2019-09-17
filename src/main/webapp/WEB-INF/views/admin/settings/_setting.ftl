{
<#if setting.id?has_content>"id" : ${setting.id},</#if>
"account_id" : "${(setting.account_id)!}",
"k" : "${(setting.k)!}",
"v" : "${(setting.v)!}",
"memo" : "${(setting.memo)!}" 
}