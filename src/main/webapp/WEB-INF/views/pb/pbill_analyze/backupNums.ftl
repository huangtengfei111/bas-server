[
  <#if pbill_records?has_content>
    <@render partial="/pb/pbill_records/pbill_record" collection=pbill_records spacer="/common/comma"/>   
  </#if>
]