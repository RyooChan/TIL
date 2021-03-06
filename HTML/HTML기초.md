## HTML이란
* HTML
    * Hyper Text Markup Language
        * 하이퍼텍스트
            * 문자를 서로 연결해주는 링크
        * 마크업
            *  표시한다 는 뜻
* HTML설명
    * DOCTYPE html
        * 현재 문서가 html5로 작성한 웹 문서라는 것을 알려주는 것이다.
    * html
        * html의 시작
    * head
        * 페이지의 속성과 정보를 설정
    * meta charset = "UTF-8"
        * 페이지를 UTF-8로 인코딩 지정
    * title
        * 웹 페이지의 제목을 나타내는 태그
            * 여기까지는 웹 페이지의 본문에는 보이지 않으며, 브라우저의 탭 등에서 확인 할 수 이있다.
    * body
        * 웹 브라우저의 실제 내용 작성하는 장소
        * h1 ~ h6
            * 제목을 나타내는데 사용
            * 참고로 h1태그는 통상 문서에서 1번만 사용한다.
        * p
            * 하나의 문단 의미
        * html은 엄청 공백이나 엔터를 많이 넣어도 하나의 공백으로 인식한다.
        * \<br/>
            * 줄바꿈 태그
        * \&lt;
            * 특수문자 '<' 표현
        * \&gt;
            * 특수문자 '>' 표현
        * \&amp;
            * 특수문자 '&' 표현
        * \&nbsp;
            * 한 칸의 공백 표현
        * pre
            * 입력한 텍스트를 그대로 보여주는 태그
            * 미리 정의된 형식의 텍스트를 정의할 때 사용
            * pre요소 내의 텍스트는 시스템에서 미리 지정된 고정폭 글꼴을 사용하여 표현
            * pre태그를 활용하면 긴 문장등을 처리할 때 빨라질 수 있다.
        * b, strong
            * 강조 태그
        * hr
            * 단일 태그(구분선)
        * i태그, em태그
            * 이텔릭체로 표시
        * mark태그
            * 형광펜으로 칠한 것처럼 하이라이트된 텍스트 표시
        * small태그
            * 텍스트를 조그맣게 표시
        * del 태그
            * 텍스트 한가운데 라인을 추가하여 삭제된 텍스트를 표현할 때 사용
        * ins 태그
            * 텍스트 아래쪽에 라인을 추가
        * sup 태그
            * 윗첨자 텍스트 표현
        * sub 태그
            * 아랫첨자 텍스트 표현
        * ul 태그
            * unordered list
            * 순서없는 목록 사용
            * li를 사용해서 나타냄
        * ol
            * ordered list
            * 순서있는 목록 사용
            * li를 사용해서 나타냄
        * <mark>테이블 태그</mark>
            * table태그에서 border속성을 이용하게 되면 표에서 테두리가 그려진다. 이 속성을 이용하게 되면 바깥 테두리 뿐만 아니라 안에 있는 셀 테두리까지 모두 그려지게 된다.
            * 행 tr
            * 열 th, td
                * th : 헤더(제목 등)
                * td : 내용들
                * colspan
                    * col(열)을 합치고자 할 때 colspan속성을 사용한다.
                    * 옆 열의 요소와 합친다.
                * rowspamn
                    * row(행)을 합치고자 할 때 rowspan속성을 사용한다.
                    * 아래 열의 요소와 합친다.
        ---
        * 인라인 속성
            * 개행하지 않고 한 줄에 다른 요소들과 함께 있으려는 속성이다.
            * 인라인 속성의 태그들은 너비/높이, 마진 등의 **CSS속성이 정상적으로 작동하지 않는다.**
            * div
        * 블록 속성
            * 하나의 태그가 그 자체로 한 줄을 완전히 차지.
            * width, height, margin등 거의 모든 CSS값들이 정상적으로 적용된다.
                * **때문에 공간을 나누거나 레이아웃을 잡을 때에는 필수적으로 block속성의 태그를 사용해야 한다.**
            * span

        ---
        * <mark>a tag</mark>
            * 하나의 페이지에서 다른 페이지로 연결할 때 사용
            * href
                * url적기
            * target
                * _blank
                    * 링크된 문서를 새로운 탭에서 열어주게 한다.
        * img 태그
            * src
                * 이미지 파일 찾아가기...절대/상대 경로가 존재한다.
            * alt
                * 이미지 파일이 없는 경우 alt에 적은 것으로 대체한다.
            * 크기 조정 가능
                * width, height 사용
            * 절대 경로
                * 시작은 해당 프로젝트부터 간다.
            * 상대 경로
                * 현재 파일의 위치로부터 파일의 위치를 찾아가면 된다.
