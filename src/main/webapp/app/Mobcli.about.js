Ext.define('Mobcli.about.AboutPanel', {
    extend: 'Ext.Panel',
    config: {
        title: 'About',
        iconCls: 'info',
        cls: 'card',
        layout: {
            type: 'vbox',
            pack: 'center',
            align: 'middle'
        },
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'About'
            },
            {
                xtype: 'img',
                src: 'image/as7.png',
                height: 140,
                width: 140,
                align: 'center'
            },
            {
                xtype: 'label',
                styleHtmlContent: 'true',
                html: '<div style="text-align: center; vertical-align: middle"><h2>Mobile CLI for JBoss AS7</h2> <b>Contact:</b> yyang@redhat.com</div>'
            }
        ]
    }
    
});