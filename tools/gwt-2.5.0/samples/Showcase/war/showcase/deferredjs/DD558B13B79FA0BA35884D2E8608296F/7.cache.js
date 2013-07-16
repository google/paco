function S7b(){T7b.call(this,false)}
function o8b(a,b){q8b.call(this,a,false);this.c=b}
function p8b(a,b){q8b.call(this,a,false);n8b(this,b)}
function r8b(a){q8b.call(this,'GWT',true);n8b(this,a)}
function byb(a){this.d=a;this.c=hib(this.d.b)}
function w7b(a,b){return D7b(a,b,a.b.c)}
function Pb(a,b){$b((ie(),de),a,QT(adb,qzc,135,[(_nc(),b?$nc:Znc)]))}
function n8b(a,b){a.e=b;!!a.d&&R7b(a.d,a);if(b){b.db.tabIndex=-1;yf();Pb(a.db,true)}else{yf();Pb(a.db,false)}}
function D7b(a,b,c){if(c<0||c>a.b.c){throw new Snc}a.p&&(b.db[SHc]=2,undefined);v7b(a,c,b.db);quc(a.b,c,b);return b}
function cib(a){var b,c;b=$T(a.b.ie(nIc),149);if(b==null){c=QT(fdb,rzc,1,[oIc,'R\xE9tablir','Couper','Copier','Coller']);a.b.ke(nIc,c);return c}else{return b}}
function dib(a){var b,c;b=$T(a.b.ie(pIc),149);if(b==null){c=QT(fdb,rzc,1,['Nouveau','Ouvrir',qIc,'R\xE9cent','Quitter']);a.b.ke(pIc,c);return c}else{return b}}
function gib(a){var b,c;b=$T(a.b.ie(tIc),149);if(b==null){c=QT(fdb,rzc,1,['Contenu','Biscuit de fortune','\xC0 propos de GWT']);a.b.ke(tIc,c);return c}else{return b}}
function fib(a){var b,c;b=$T(a.b.ie(sIc),149);if(b==null){c=QT(fdb,rzc,1,['T\xE9l\xE9charger','Exemples',GEc,'GWiTtez avec le programme']);a.b.ke(sIc,c);return c}else{return b}}
function eib(a){var b,c;b=$T(a.b.ie(rIc),149);if(b==null){c=QT(fdb,rzc,1,['P\xEAcher dans le d\xE9sert.txt','Comment apprivoiser un perroquet sauvage',"L'\xE9levage des \xE9meus pour les nuls"]);a.b.ke(rIc,c);return c}else{return b}}
function u8b(){var a;Ni(this,$doc.createElement(aGc));this.db[lCc]='gwt-MenuItemSeparator';a=$doc.createElement(qCc);mWb(this.db,a);a[lCc]='menuSeparatorInner'}
function hib(a){var b,c;b=$T(a.b.ie(uIc),149);if(b==null){c=QT(fdb,rzc,1,["Merci d'avoir s\xE9lectionn\xE9 une option de menu",'Une s\xE9lection vraiment pertinente',"N'avez-vous rien de mieux \xE0 faire que de s\xE9lectionner des options de menu?","Essayez quelque chose d'autre","ceci n'est qu'un menu!",'Un autre clic gaspill\xE9']);a.b.ke(uIc,c);return c}else{return b}}
function Zxb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new byb(a);n=new S7b;n.c=true;n.db.style[mCc]='500px';n.f=true;q=new T7b(true);p=eib(a.b);for(k=0;k<p.length;++k){u7b(q,new o8b(p[k],o))}d=new T7b(true);d.f=true;u7b(n,new p8b('Fichier',d));e=dib(a.b);for(k=0;k<e.length;++k){if(k==3){w7b(d,new u8b);u7b(d,new p8b(e[3],q));w7b(d,new u8b)}else{u7b(d,new o8b(e[k],o))}}b=new T7b(true);u7b(n,new p8b('\xC9dition',b));c=cib(a.b);for(k=0;k<c.length;++k){u7b(b,new o8b(c[k],o))}f=new T7b(true);u7b(n,new r8b(f));g=fib(a.b);for(k=0;k<g.length;++k){u7b(f,new o8b(g[k],o))}i=new T7b(true);w7b(n,new u8b);u7b(n,new p8b('Aide',i));j=gib(a.b);for(k=0;k<j.length;++k){u7b(i,new o8b(j[k],o))}Vgc(n.db,MBc,vIc);Q7b(n,vIc);return n}
var vIc='cwMenuBar',nIc='cwMenuBarEditOptions',pIc='cwMenuBarFileOptions',rIc='cwMenuBarFileRecents',sIc='cwMenuBarGWTOptions',tIc='cwMenuBarHelpOptions',uIc='cwMenuBarPrompts';heb(683,1,{},byb);_.oc=function cyb(){cXb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;heb(684,1,eAc);_.mc=function gyb(){Mgb(this.c,Zxb(this.b))};heb(1078,102,tzc,S7b);heb(1085,103,{100:1,105:1,119:1},o8b,p8b,r8b);heb(1086,103,{100:1,106:1,119:1},u8b);var s2=uoc(UGc,'CwMenuBar$1',683),e8=uoc(SGc,'MenuItemSeparator',1086);TAc(wn)(7);