Ext.ns('Mobcli');

Ext.require('Ext.tab.Panel');

Ext.application({
    name: 'Mobile CLI for JBoss AS7',
    launch: function() {
        var inputNavigationView = Ext.create("Mobcli.input.NavigationView", 
            {
                id: 'inputNavigationView',
                cls: 'card dark',
                iconCls: 'search',
                items: [
                    Ext.create('Mobcli.input.NodeListView',
                        {
                            title: 'PATH:',
                            address: '~',
                            store: {
                                data: [
                                    Ext.create('Mobcli.input.NodeModel', {
                                        name: '/', 
                                        address: '/', 
                                        value: '/', 
                                        displayname: '/', 
                                        leaf: false,
                                        generic: false}).getData()
                                ]
                            }
                        })            
                ]
            });
        
        var outputPanel = Ext.create('Mobcli.output.Panel', 
            {
                id: 'ID_OutputPanel',
                title: 'Output',
                cls: 'card dark',
                iconCls: 'download'
            });

        var tabPanel =  Ext.create("Ext.tab.Panel", {
                id: 'mainTabPanel',
                ui: 'dark',
                tabBar: {
//                    ui: Ext.filterPlatform('blackberry') || Ext.filterPlatform('ie10') ? 'dark' : 'light',
                    layout: {
                        pack : 'center',
                        align: 'center'
                    },
                    docked: 'bottom'
                },
                defaults: {
                    scrollable: true
                },
                items: [
                    inputNavigationView,
                    outputPanel,
                    Ext.create('Mobcli.monitor.MonitorPanel', {
                        cls: 'card dark',
                        iconCls: 'time'                        
                    }),
                    Ext.create('Mobcli.setting.SettingPanel', {
                        cls: 'card dark',
                        iconCls: 'settings'
                    }),
                    Ext.create('Mobcli.about.AboutPanel')
                ]
                });

        Ext.Viewport.add(tabPanel);
    }
});
