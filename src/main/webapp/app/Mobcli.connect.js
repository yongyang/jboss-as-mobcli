Ext.define('Mobcli.connect.ConnectionPanel', {
    extend: 'Ext.Panel',
    config: {
        title: 'Connection',
        layout: {
            type: 'vbox',
            pack: 'center',
            align: 'middle'
        },
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Connection'
            },
            {
                xtype: 'formpanel',
                items: [
                    {
                        xtype: 'text',
                        label: 'Host'                        
                    },
                    {
                        xtype: 'text',
                        label: 'Port'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                titile: 'connect'
                            },
                            {
                                xtype: 'button',
                                title: 'reset'
                            }
                        ]
                    }
                ]
            }
        ]
    }
});