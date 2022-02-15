* createElement를 사용해서 만든 html태그 내에서
  * 다른 변수로 현재 값을 보내는 방법
  
<pre>
만들어진것.onclick = (function(index) {
    return function() {
        변수를받을함수(index);
    };
}(보낼변수));
</pre>

이렇게 하면 for문 사용시 현재 변수를 다른 함수로 보낼 수 있다.
