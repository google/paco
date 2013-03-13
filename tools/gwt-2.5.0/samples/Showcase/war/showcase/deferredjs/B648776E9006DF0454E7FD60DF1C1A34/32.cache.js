function wD(){wD=umc;vD=new Zjc}
function RTb(a,b,c,d){var e;a.b.Wf(b,c);e=TTb(a.b.j,b,c);dj(e,d,true)}
function xD(d,a){var b=d.b;for(var c in b){b.hasOwnProperty(c)&&a.ld(c)}}
function zD(d,a){a=String(a);var b=d.b;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.bd(a);return String(c)}
function BD(){wD();var a;a=DH(vD.fd(jyc),61);if(!a){a=new AD;vD.hd(jyc,a)}return a}
function yD(c,b){try{typeof $wnd[b]!='object'&&DD(b);c.b=$wnd[b]}catch(a){DD(b)}}
function DD(a){throw new $kc(erc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function AD(){this.c='Dictionary userInfo';yD(this,jyc);if(!this.b){throw new $kc("Cannot find JavaScript object with the name 'userInfo'")}}
function Qjb(){var a,b,c,d,e,f,g,i,j,k,n;f=new B4b;g=new cRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.db.dir=zqc;g.db.style['textAlign']=Tqc;y4b(f,new cRb('<b>\u8FD9\u4E2A\u4F8B\u5B50\u4F7F\u7528\u4E0B\u5217Javascript\u7684\u53D8\u91CF\uFF1A <\/b>'));y4b(f,g);j=new JTb;b=j.k;i=BD();e=(n=new fkc,xD(i,n),n);a=0;for(d=bhc(CE(e.b));d.b.rd();){c=DH(hhc(d),1);k=zD(i,c);ATb(j,0,a,c);RTb(b,0,a,'cw-DictionaryExample-header');ATb(j,1,a,k);RTb(b,1,a,'cw-DictionaryExample-data');++a}y4b(f,new cRb('<br><br>'));y4b(f,j);return f}
var jyc='userInfo';s1(344,1,{61:1},AD);_.bd=function CD(a){var b;b="Cannot find '"+a+"' in "+this;throw new $kc(b)};_.tS=function ED(){return this.c};_.b=null;_.c=null;var vD;s1(642,1,onc);_.mc=function Wjb(){X3(this.b,Qjb())};var UM=Ebc(muc,'Dictionary',344);boc(wn)(32);