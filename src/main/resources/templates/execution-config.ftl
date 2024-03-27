[@ui.bambooSection titleKey="com.simplifyqa.fields.titleKey" ]

    [@ww.password   labelKey="com.simplifyqa.fields.token.labelKey" 
                    name="com.simplifyqa.fields.token.nameKey" 
                    showPassword='true'
                    required='true' /]

    [@ww.textfield  labelKey="com.simplifyqa.fields.url.labelKey" 
                    name="com.simplifyqa.fields.url.nameKey" 
                    required='true' /]
    
    [@ww.checkbox   labelKey='com.simplifyqa.fields.advancedCheck'
                    name='com.simplifyqa.fields.advancedCheck'
                    toggle='true' /]

    [@ui.bambooSection  dependsOn='com.simplifyqa.fields.advancedCheck'
                        showOn='true']
        
        [@ww.textfield  labelKey='com.simplifyqa.fields.threshold.labelKey'
                        name='com.simplifyqa.fields.threshold.nameKey' /]

        [@ww.checkbox   labelKey='com.simplifyqa.fields.verbose.labelKey'
                        name='com.simplifyqa.fields.verbose.nameKey' /]

    [/@ui.bambooSection]

[/@ui.bambooSection]