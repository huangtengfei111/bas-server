{
<#if citizen.id?has_content>"id" : ${citizen.id},</#if>
"social_no" : "${(citizen.social_no)!}",
"name" : "${(citizen.name)!}",
"address" : "${(citizen.address)!}",
"phone" : "${(citizen.phone)!}",
"company" : "${(citizen.company)!}",
"mobile" : "${(citizen.mobile)!}"
}