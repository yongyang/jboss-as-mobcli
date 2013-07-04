Ext.ns('Mobcli.connect');

Ext.require(
    [
        'Ext.MessageBox'
    ]
);
Ext.define('Mobcli.connect.ConnectionPanel', {
    extend: 'Ext.Panel',
    config: {
        title: 'Connection',
        layout: {
            type: 'fit'
        },
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Connection'
            },
            {
                xtype: 'formpanel',
                id: 'ID_connectFormPanel',
                items: [
                    {
                        xtype: 'fieldset',
                        title: 'Please input necessary info for connection:',
                        instructions: 'NOTE: Address localhost/127.0.0.1 means the server running servlet container',
                        items: [

                            {
                                xtype: 'textfield',
                                id: 'ID_host',
                                name: 'host',
                                label: 'Host',
                                value: '127.0.0.1',
                                placeHolder: 'The server ip/name you want to manage'
                            },
                            {
                                xtype: 'numberfield',
                                id: 'ID_port',
                                name: 'port',
                                label: 'Port',
                                value: '9999',
                                placeHolder: 'The Management port, default 9999'
                            },
                            {
                                xtype: 'textfield',
                                id: 'ID_user',
                                name: 'user',
                                label: 'User',
                                placeHolder: 'User name used to manage server'
                            },
                            {
                                xtype: 'passwordfield',
                                id: 'ID_password',
                                name: 'password',
                                label: 'Password',
                                placeHolder: 'Password of the user'
                            }
                            
                        ]                    
                    
                    },                    
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        padding: 5,
                        defaults: {
                            margin: 5
                        },
                        items: [
                            {
                                xtype: 'button',
                                text: 'connect',
                                handler: function() {
                                    Ext.Viewport.mask({
                                        xtype: 'loadmask',
                                        message: 'connect...'
                                    });
                                    var values = Ext.getCmp('ID_connectFormPanel').getValues();
                                    Ext.Ajax.request({
                                        url : 'cliservlet/connect',
                                        params  : values,
                                        method: 'GET',
                                        success: function(response, opts) {
                                            var topPanel = Ext.getCmp('ID_topPanel');
                                            topPanel.animateActiveItem(1, {type: 'pop', direction: 'left', duration: 200});
                                        },
                                        failure: function(response, opts) {
                                            Ext.Msg.alert('Failure', 'Failed to connect ' + Ext.getCmp('ID_host').getValue() + ":" + Ext.getCmp('ID_port').getValue());
                                        },
                                        callback: function() {
                                            Ext.Viewport.unmask();
                                        }
                                        
                                    });

                                }
                            },
                            {
                                xtype: 'button',
                                text: 'reset'
                            }
                        ]
                    }
                ]
            }
        ]
    }
});
