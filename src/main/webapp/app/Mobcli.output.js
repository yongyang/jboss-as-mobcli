Ext.define('Mobcli.output.Panel', {
    extend: 'Ext.Panel',
    config: {
        layout: 'vbox',
        scrollable: true,
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Output'
            }
        ]
    },
    print: function(operation, operationResult) {
        var panel = Ext.create('Ext.Panel',{
            items: [
                {
                    xtype: 'label',
                    html: operation + "<br></pre>" + operationResult + "</pre>"
                }                       
            ]
        });
        this.add(panel);
    }
});