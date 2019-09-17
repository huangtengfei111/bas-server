[<#if pbill_records?has_content>
  <#list pbill_records as pbill_record>
  <#assign sameCIInCases = pbill_record.suggestionsONSameCIInCase(case_id, pbill_record.owner_ci)! />
  <#assign cityAndCIs = pbill_record.suggestionsOnCityAndCI(pbill_record.owner_comm_loc ,pbill_record.owner_ci)! />
  
    {
      "owner_comm_loc" : "${pbill_record.owner_comm_loc!""}",
      "owner_ct_code" : "${pbill_record.owner_ct_code!""}",
      "sugs_same_ci" : [
                          <#if sameCIInCases?has_content>
                            <#list sameCIInCases as sameCIIncase >
                              "${sameCIIncase}"<#sep>,</#sep>
                            </#list>
                          </#if>
                        ],
      
      "sugs_city_ci" : [
                         <#if cityAndCIs?has_content>
                            <#list cityAndCIs as cityAndCI >
                              "${cityAndCI.code}"<#sep>,</#sep>
                            </#list>
                         </#if>
                        ]
    }<#sep>,</#sep>
  </#list>
</#if>
]