Ext.ns('Mobcli.input');

Ext.define('Mobcli.input.NodeModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {name: 'address', type: 'string'}, // full address
            {name: 'name', type: 'string'}, // name
            {name: 'leaf', type: 'boolean'} // is path or property, path is not leaf
        ]
    }
});

Ext.define("Mobcli.input.NodeLoaderStore", {
    extend: 'Ext.data.Store',
    config: {
        model: 'Mobcli.input.NodeModel',
        address: "/",
        sorters: 'name',
        autoLoad: false,
        grouper: function(record) {
            return record.get('leaf') ? "Property" : "Path";
        },
        proxy: {
            type: 'ajax',
            url: 'cliservlet/resource',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.children'
            },

            listeners : {
                exception : function(proxy, response, operation) {
                    console.log(response);
                }
            }
        }
    }
});

Ext.define('Mobcli.NodeList', {
        extend: 'Ext.List',
        xtype: 'moblci_nodelist',
        config: {
            address: '/', // default address /, should be set for different address
            grouper: function(record) {
                return record.get('ispath') ? "Property" : "Path";
            },
            masked: {
                xtype: 'loadmask',
                message: 'loading...'
            },
            itemTpl: '<div>{name}</div>',
            onItemDisclosure: true,
            listeners: {
                scope: this,
                itemtap: function() {
                    Ext.Msg.alert("Tap", "Tap");
                },
                disclose: function(el, record, target, index, e, eOpts ) {
                    e.stopEvent();
                    Ext.getCmp('inputNavigationView').push({
                        title: 'Commands',
                        html: 'Command List'
                    });
                    return false;
                }
            }
        },

        constructor: function(config) {
            config = config?  config : {};
            this.callParent(config);
            this.initConfig(config);
            var store = Ext.create("Mobcli.input.NodeLoaderStore",{address: this.getAddress()});
            store.getProxy().setUrl(store.getProxy().getUrl() + "?addr=" + Ext.Object.toQueryString(this.getAddress()));
            store.getProxy().setExtraParams('addr', this.getAddress());
            this.setStore(store);
            console.log(store.getProxy());
            store.load();
            return this;
        }
    }
);