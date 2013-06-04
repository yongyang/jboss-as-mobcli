Ext.ns('Mobcli.output');

Ext.define('Mobcli.output.Panel', {
    extend: 'Ext.Panel',
    config: {
        layout: 'vbox',
        scrollable: 'both',
        defaults: {
            padding: 5
        },
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Output'
            }
        ]
    },
    initialize: function() {
        this.callParent();
        // scroll to end automatically
        this.getScrollable().getScroller().on('maxpositionchange', function(scroller, max) {
            scroller.scrollTo(0, max.y);
        });
    },
    print: function(operationResultJSON) {
        this.add(Ext.create('Ext.Label',
                {
                    html: operationResultJSON.command + "<br><pre>" + operationResultJSON.result + "</pre> <hr>"
                }
        ));
    }
});