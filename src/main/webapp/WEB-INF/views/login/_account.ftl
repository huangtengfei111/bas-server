<#if account?has_content>
{
	"user_id" : ${account.user.id},
	"username": "${account.username}",
	"name": "${(account.user.name)!}",
	"role": "${account.role.value}",
	"system_id" : "${system_id!}"
}
<#else>
[]
</#if>