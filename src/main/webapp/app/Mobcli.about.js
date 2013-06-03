Ext.define('Mobcli.about.AboutPanel', {
    extend: 'Ext.Panel',
    config: {
        title: 'About',
        iconCls: 'info',
        cls: 'card',
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'About'
            },
            {
                xtype: 'label',
                styleHtmlContent: 'true',
                html: '<div style="text-align: center; vertical-align: middle"><h2>Mobile CLI for JBoss AS7</h2> <b>Contact:</b> yyang@redhat.com</div>'
            }
        ]
    }
    
});