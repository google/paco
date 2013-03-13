function tG(){tG=K2c;sG=new n0c}
function uG(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ve(c)}}
function Ozc(a,b,c,d){var e;a.b.eh(b,c);e=Qzc(a.b.j,b,c);pj(e,d,true)}
function yG(){tG();var a;a=Qlb(sG.qe(nfd),61);if(!a){a=new xG;sG.se(nfd,a)}return a}
function vG(c,b){try{typeof $wnd[b]!='object'&&AG(b);c.b=$wnd[b]}catch(a){AG(b)}}
function AG(a){throw new o1c(B7c+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function wG(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Zd(a);return String(c)}
function xG(){this.c='Dictionary userInfo';vG(this,nfd);if(!this.b){throw new o1c("Cannot find JavaScript object with the name 'userInfo'")}}
function M_b(){var a,b,c,d,e,f,g,i,j,k,n;f=new BMc;g=new dxc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=$6c;g.db.style['textAlign']=o7c;yMc(f,new dxc('<b>This example interacts with the following JavaScript variable:<\/b>'));yMc(f,g);j=new Gzc;b=j.k;i=yG();e=(n=new v0c,uG(i,n),n);a=0;for(d=rZc(SN(e.b));d.b.Be();){c=Qlb(xZc(d),1);k=wG(i,c);xzc(j,0,a,c);Ozc(b,0,a,'cw-DictionaryExample-header');xzc(j,1,a,k);Ozc(b,1,a,'cw-DictionaryExample-data');++a}yMc(f,new dxc('<br><br>'));yMc(f,j);return f}
var nfd='userInfo';oJb(369,1,{61:1},xG);_.Zd=function zG(a){var b;b="Cannot find '"+a+"' in "+this;throw new o1c(b)};_.tS=function BG(){return this.c};_.b=null;_.c=null;var sG;oJb(730,1,F3c);_.qc=function S_b(){TLb(this.b,M_b())};var Erb=TTc(ibd,J8c,369);s4c(Jn)(32);