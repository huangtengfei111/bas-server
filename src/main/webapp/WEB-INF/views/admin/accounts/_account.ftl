{
<#if account.id?has_content>"id" : ${account.id},</#if>
"role" : "${(account.role.name)!}",
"name" : "${(account.user.name)!}",
"memo" : "${(account.user.memo)!}",
"built_in": ${(account.builtIn?then("true", "false"))},
"username" : "${(account.username)!}",
"last_login_at" : "${(account.user.lastLoginAt)!}",
"last_remote_host" : "${(account.user.lastRemoteHost)!}"
}