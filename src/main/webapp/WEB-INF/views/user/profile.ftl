{
<#if account?has_content>
"username" : "${(account.username)!}",
"name" : "${(account.user.name)!}",
"avatar" : "${(account.user.avatar)!}" ,
"last_login_at" : "${(account.user.lastLoginAt)!}",
"last_remote_host" : "${(account.user.lastRemoteHost)!}"
</#if>
}