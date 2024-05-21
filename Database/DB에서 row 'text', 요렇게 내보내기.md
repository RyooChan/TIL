## DB에서 row 'text', 요렇게 내보내기

```
select concat('\'', data, '\',') from table;
```

요렇게 하면 됨
