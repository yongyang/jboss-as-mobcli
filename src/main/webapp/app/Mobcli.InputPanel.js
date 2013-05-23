Ext.define('Mobcli.TestModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: ['firstName', 'lastName']
    }
});

Ext.define("Mobcli.TestStore", {
    extend: 'Ext.data.Store',
    config: {
        model: 'Mobcli.TestModel',
        sorters: 'firstName',
        grouper: function(record) {
            return record.get('firstName')[0];
        },
        data: [
            {firstName: 'Julio', lastName: 'Benesh'},
            {firstName: 'Julio', lastName: 'Minich'},
            {firstName: 'Tania', lastName: 'Ricco'},
            {firstName: 'Odessa', lastName: 'Steuck'},
            {firstName: 'Nelson', lastName: 'Raber'},
            {firstName: 'Tyrone', lastName: 'Scannell'},
            {firstName: 'Allan', lastName: 'Disbrow'},
            {firstName: 'Cody', lastName: 'Herrell'},
            {firstName: 'Julio', lastName: 'Burgoyne'},
            {firstName: 'Jessie', lastName: 'Boedeker'},
            {firstName: 'Allan', lastName: 'Leyendecker'},
            {firstName: 'Javier', lastName: 'Lockley'},
            {firstName: 'Guy', lastName: 'Reasor'},
            {firstName: 'Jamie', lastName: 'Brummer'},
            {firstName: 'Jessie', lastName: 'Casa'},
            {firstName: 'Marcie', lastName: 'Ricca'},
            {firstName: 'Gay', lastName: 'Lamoureaux'},
            {firstName: 'Althea', lastName: 'Sturtz'},
            {firstName: 'Kenya', lastName: 'Morocco'},
            {firstName: 'Rae', lastName: 'Pasquariello'},
            {firstName: 'Ted', lastName: 'Abundis'},
            {firstName: 'Jessie', lastName: 'Schacherer'},
            {firstName: 'Jamie', lastName: 'Gleaves'},
            {firstName: 'Hillary', lastName: 'Spiva'},
            {firstName: 'Elinor', lastName: 'Rockefeller'},
            {firstName: 'Zebra', lastName: 'Evilias'}
        ]
    }
});

/*
var l = Ext.create("Ext.List", {
    store:  Ext.create("Mobcli.TestStore"),
    itemTpl: '<div>{lastName} - {firstName}</div>',
    onItemDisclosure: function(record, btn, index) {
        Ext.Msg.alert('Tap', 'Disclose more info of ' + index + ', ' + record);
    }
});

*/

Ext.define('Mobcli.NodeList', {
        extend: 'Ext.List',
        xtype: 'moblci_nodelist',
        config: {
            store:  Ext.create("Mobcli.TestStore"),
            itemTpl: '<div>{lastName} - {firstName}</div>',
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
            this.initConfig(config)
        }
    }
);


Ext.define('Mobcli.NodeNestedList', {
        extend: 'Ext.NestedList',
        xtype: 'moblci_nestednodelist',
        config: {
            store:  Ext.create("Mobcli.TestStore"),
            itemTpl: '<div>{lastName} - {firstName}</div>',
            displayField: 'name',
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
            },

            masked: {
                xtype: 'loadmask',
                message: 'loading...'
            }

        },

        constructor: function(config) {
            config = config?  config : {};
            this.callParent(config);
            this.initConfig(config)
        }
    }
);

Ext.define('Mobcli.NodeModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {name: 'address', type: 'string'}, // full address
            {name: 'name', type: 'string'}, // name
            {name: 'ispath', type: 'boolean'} // is path or property, path is not leaf
        ]
    }
});

Ext.define('Mobcli.TreeStore', {
    extend: 'Ext.data.TreeStore',
    model: 'Mobcli.ListModel',
    sorters: 'name',
    grouper: function(record) {
        return record.get('ispath') ? "Property" : "Path";
    },
    autoLoad: 'true',
    proxy: {
        url: 'cliservlet/resource',
        type: 'ajax',
        reader: {
            type: 'json'
        }
    }
});
